package com.example.evcharging.simulator;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.*;
import java.util.regex.*;

/**
 * Stateful simulator that preserves the local transaction across TCP reconnects.
 * While disconnected, the vehicle continues charging locally and the meter keeps increasing.
 */
public final class DeviceSimulator {
    private static final Pattern SESSION=Pattern.compile("\\\"sessionNo\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"");
    private static final Pattern CONNECTOR=Pattern.compile("\\\"connectorNo\\\"\\s*:\\s*(\\d+)");

    public static void main(String[] args) throws Exception {
        long tenant=args.length>0?Long.parseLong(args[0]):1L;
        String device=args.length>1?args[1]:"CP000001";
        String host=System.getenv().getOrDefault("IOT_HOST","127.0.0.1");
        int port=Integer.parseInt(System.getenv().getOrDefault("IOT_PORT","19090"));
        String secret=System.getenv().getOrDefault("IOT_DEV_SECRET","dev-secret");
        State state=new State();
        state.alarmAfterSeconds=Integer.parseInt(System.getenv().getOrDefault("SIM_ALARM_AFTER_SECONDS","0"));
        state.alarmRecoverAfterSeconds=Integer.parseInt(System.getenv().getOrDefault("SIM_ALARM_RECOVER_AFTER_SECONDS","0"));
        state.alarmCode=System.getenv().getOrDefault("SIM_ALARM_CODE","CONNECTOR_OVER_TEMPERATURE");
        state.alarmSeverity=System.getenv().getOrDefault("SIM_ALARM_SEVERITY","CRITICAL");
        state.alarmValue=System.getenv().getOrDefault("SIM_ALARM_VALUE","85");
        state.alarmUnit=System.getenv().getOrDefault("SIM_ALARM_UNIT","C");
        Thread.ofPlatform().name("simulator-local-meter").start(()->localMeterLoop(state));
        while(true){
            try { connectOnce(tenant,device,host,port,secret,state); }
            catch(Exception e){System.err.println("connection lost: "+e.getMessage());}
            state.out.set(null);
            Thread.sleep(2000);
        }
    }

    private static void connectOnce(long tenant,String device,String host,int port,String secret,State state)throws Exception{
        try(Socket socket=new Socket(host,port);
            BufferedReader in=new BufferedReader(new InputStreamReader(socket.getInputStream(),StandardCharsets.UTF_8));
            BufferedWriter out=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),StandardCharsets.UTF_8))){
            state.out.set(out);
            AtomicBoolean connected=new AtomicBoolean(true);
            send(out,"AUTH|"+tenant+"|"+device+"|"+secret);
            Thread heartbeat=Thread.ofVirtual().start(()->heartbeatLoop(out,connected));
            String line;
            while((line=in.readLine())!=null){
                System.out.println("< "+line);
                handle(line,out,state);
            }
            connected.set(false); heartbeat.join();
        }
    }

    private static void handle(String line,BufferedWriter out,State state)throws IOException{
        if(!line.startsWith("COMMAND|"))return;
        String[] p=line.split("\\|",4); String commandId=p[1],type=p[2],payload=p.length>3?p[3]:"{}";
        if(!state.commands.add(commandId)){send(out,"COMMAND_ACK|"+commandId+"|DUPLICATE|"+type);return;}
        send(out,"COMMAND_ACK|"+commandId+"|SUCCESS|"+type);
        if("START_CHARGING".equals(type)){
            state.session.set(extract(SESSION,payload,"UNKNOWN")); state.connector.set(Integer.parseInt(extract(CONNECTOR,payload,"1")));
            state.startMeter.set(state.meter.get()); state.startSoc.set(state.soc.get()); state.startOccurredMs.set(System.currentTimeMillis());
            state.stopOccurredMs.set(0);
            state.alarmRaised.set(false); state.alarmRecovered.set(false);
            state.charging.set(true); sendStarted(out,state);
        }else if("STOP_CHARGING".equals(type)){
            state.charging.set(false); state.stopOccurredMs.set(System.currentTimeMillis()); sendStopped(out,state,"USER");
        }else if("QUERY_TRANSACTION".equals(type)){
            if(state.session.get()==null)return;
            if(state.charging.get())sendStarted(out,state); else sendStopped(out,state,"RECOVERY_REPLAY");
        }
    }

    private static void localMeterLoop(State state){
        while(true){
            try{
                if(state.charging.get()){
                    long m=state.meter.addAndGet(500); int s=Math.min(99,state.soc.incrementAndGet());
                    BufferedWriter out=state.out.get();
                    if(out!=null){
                        try{
                            long now=System.currentTimeMillis();
                            send(out,"TELEMETRY|"+state.session.get()+"|"+state.connector.get()+"|"+s+"|60000|"+m+"|"+now);
                            long elapsedSeconds=state.startOccurredMs.get()>0?(now-state.startOccurredMs.get())/1000:0;
                            if(state.alarmAfterSeconds>0 && elapsedSeconds>=state.alarmAfterSeconds && state.alarmRaised.compareAndSet(false,true)){
                                send(out,"ALARM|"+state.alarmCode+"|"+state.alarmSeverity+"|"+state.connector.get()+"|"+state.alarmValue+"|"+state.alarmUnit+"|simulated_alarm|"+now);
                            }
                            if(state.alarmRaised.get() && state.alarmRecoverAfterSeconds>0
                                    && elapsedSeconds>=state.alarmRecoverAfterSeconds
                                    && state.alarmRecovered.compareAndSet(false,true)){
                                send(out,"ALARM_RECOVERED|"+state.alarmCode+"|"+state.connector.get()+"|"+now);
                            }
                        }catch(IOException ignored){state.out.compareAndSet(out,null);}
                    }
                }
                Thread.sleep(1000);
            }catch(InterruptedException e){Thread.currentThread().interrupt();return;}
        }
    }

    private static void heartbeatLoop(BufferedWriter out,AtomicBoolean connected){
        try{while(connected.get()){send(out,"PING|"+Instant.now().toEpochMilli());Thread.sleep(5000);}}
        catch(Exception ignored){connected.set(false);}
    }

    private static void sendStarted(BufferedWriter out,State s)throws IOException{
        send(out,"CHARGING_STARTED|"+s.session.get()+"|"+s.connector.get()+"|"+s.startMeter.get()+"|"+s.startSoc.get()+"|"+s.startOccurredMs.get());
    }
    private static void sendStopped(BufferedWriter out,State s,String reason)throws IOException{
        long ts=s.stopOccurredMs.get()>0?s.stopOccurredMs.get():System.currentTimeMillis();
        send(out,"CHARGING_STOPPED|"+s.session.get()+"|"+s.connector.get()+"|"+s.meter.get()+"|"+s.soc.get()+"|"+reason+"|"+ts);
    }
    private static String extract(Pattern p,String input,String fallback){Matcher m=p.matcher(input);return m.find()?m.group(1):fallback;}
    private static synchronized void send(BufferedWriter out,String line)throws IOException{System.out.println("> "+line);out.write(line);out.write("\n");out.flush();}

    private static final class State{
        final AtomicBoolean charging=new AtomicBoolean(false);
        final AtomicLong meter=new AtomicLong(100000),startMeter=new AtomicLong(100000),startOccurredMs=new AtomicLong(0),stopOccurredMs=new AtomicLong(0);
        final AtomicInteger soc=new AtomicInteger(20),startSoc=new AtomicInteger(20),connector=new AtomicInteger(1);
        final AtomicReference<String> session=new AtomicReference<>();
        final AtomicReference<BufferedWriter> out=new AtomicReference<>();
        final AtomicBoolean alarmRaised=new AtomicBoolean(false),alarmRecovered=new AtomicBoolean(false);
        int alarmAfterSeconds,alarmRecoverAfterSeconds;
        String alarmCode,alarmSeverity,alarmValue,alarmUnit;
        final Set<String> commands=ConcurrentHashMap.newKeySet();
    }
}
