package com.example.evcharging.iot.gateway;

import com.example.evcharging.framework.contract.DeviceChargingEvent;
import com.example.evcharging.framework.contract.DeviceAlarmEvent;
import com.example.evcharging.framework.contract.DeviceLifecycleEvent;
import com.example.evcharging.framework.contract.DeviceRouteLease;
import com.example.evcharging.framework.event.DomainEventEnvelope;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.evcharging.iot.lifecycle.DeviceHeartbeatDeadlineRegistry;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.AttributeKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.SmartLifecycle;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class NettyDeviceServer implements SmartLifecycle {
    private final int port;
    private final String gatewayId;
    private final StringRedisTemplate redis;
    private final DeviceChannelRegistry registry;
    private final String devSecret;
    private final KafkaTemplate<String,String> kafka;
    private final ObjectMapper mapper;
    private final DeviceHeartbeatDeadlineRegistry heartbeatDeadlines;
    private volatile boolean running;
    private EventLoopGroup boss, worker;
    private Channel serverChannel;

    public NettyDeviceServer(@Value("${iot.tcp-port:19090}") int port,
                             @Value("${iot.gateway-id:dev}") String gatewayId,
                             @Value("${iot.dev-secret:dev-secret}") String devSecret,
                             StringRedisTemplate redis,
                             DeviceChannelRegistry registry,
                             KafkaTemplate<String,String> kafka,
                             ObjectMapper mapper,
                             DeviceHeartbeatDeadlineRegistry heartbeatDeadlines) {
        this.port=port; this.gatewayId=gatewayId; this.devSecret=devSecret; this.redis=redis;
        this.registry=registry; this.kafka=kafka; this.mapper=mapper; this.heartbeatDeadlines=heartbeatDeadlines;
    }

    @Override public synchronized void start() {
        if (running) return;
        boss=new NioEventLoopGroup(1); worker=new NioEventLoopGroup();
        try {
            serverChannel=new ServerBootstrap().group(boss,worker).channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(new LineBasedFrameDecoder(8192),
                                    new StringDecoder(StandardCharsets.UTF_8), new StringEncoder(StandardCharsets.UTF_8),
                                    new DeviceProtocolHandler(gatewayId, redis, registry, devSecret, kafka, mapper, heartbeatDeadlines));
                        }
                    }).bind(port).syncUninterruptibly().channel();
            running=true;
        } catch (RuntimeException e) { stop(); throw e; }
    }

    @Override public synchronized void stop() {
        running=false;
        if (serverChannel!=null) serverChannel.close().syncUninterruptibly();
        if (worker!=null) worker.shutdownGracefully();
        if (boss!=null) boss.shutdownGracefully();
    }
    @Override public boolean isRunning(){return running;}
}

final class DeviceProtocolHandler extends SimpleChannelInboundHandler<String> {
    private static final Logger log=LoggerFactory.getLogger(DeviceProtocolHandler.class);
    private static final AttributeKey<Long> TENANT_ID=AttributeKey.valueOf("tenantId");
    private static final AttributeKey<String> DEVICE_ID=AttributeKey.valueOf("deviceId");
    private static final AttributeKey<String> CONNECTION_LEASE=AttributeKey.valueOf("connectionLease");
    private static final Duration ONLINE_TTL=Duration.ofSeconds(90);
    private static final DefaultRedisScript<Long> RELEASE_LEASE = new DefaultRedisScript<>("""
            local removed = 0
            if redis.call('get', KEYS[1]) == ARGV[1] then redis.call('del', KEYS[1]); removed = removed + 1 end
            if redis.call('get', KEYS[2]) == ARGV[1] then redis.call('del', KEYS[2]); removed = removed + 1 end
            return removed
            """, Long.class);

    private final String gatewayId;
    private final StringRedisTemplate redis;
    private final DeviceChannelRegistry registry;
    private final String devSecret;
    private final KafkaTemplate<String,String> kafka;
    private final ObjectMapper mapper;
    private final DeviceHeartbeatDeadlineRegistry heartbeatDeadlines;

    DeviceProtocolHandler(String gatewayId, StringRedisTemplate redis, DeviceChannelRegistry registry, String devSecret,
                          KafkaTemplate<String,String> kafka, ObjectMapper mapper,
                          DeviceHeartbeatDeadlineRegistry heartbeatDeadlines) {
        this.gatewayId=gatewayId; this.redis=redis; this.registry=registry; this.devSecret=devSecret; this.kafka=kafka; this.mapper=mapper;
        this.heartbeatDeadlines=heartbeatDeadlines;
    }

    @Override protected void channelRead0(ChannelHandlerContext ctx,String message) {
        String[] p=message.trim().split("\\|",8);
        if(p.length==0)return;
        switch(p[0]) {
            case "AUTH" -> auth(ctx,p);
            case "PING" -> ping(ctx);
            case "TELEMETRY" -> telemetry(ctx,p);
            case "CHARGING_STARTED" -> started(ctx,p);
            case "CHARGING_STOPPED" -> stopped(ctx,p);
            case "ALARM" -> alarm(ctx,p);
            case "ALARM_RECOVERED" -> alarmRecovered(ctx,p);
            case "COMMAND_ACK" -> ctx.writeAndFlush("COMMAND_ACK_RECEIVED|"+(p.length>1?p[1]:"UNKNOWN")+"\n");
            default -> ctx.writeAndFlush("ERR|UNKNOWN_MESSAGE\n");
        }
    }

    private void auth(ChannelHandlerContext ctx,String[] p) {
        if(p.length<4){ctx.writeAndFlush("ERR|BAD_AUTH\n");return;}
        long tenantId;
        try{tenantId=Long.parseLong(p[1]);}catch(Exception e){ctx.writeAndFlush("ERR|BAD_TENANT\n");return;}
        if(!MessageDigest.isEqual(devSecret.getBytes(StandardCharsets.UTF_8),p[3].getBytes(StandardCharsets.UTF_8))) {
            ctx.writeAndFlush("ERR|AUTH_FAILED\n"); ctx.close(); return;
        }
        String deviceId=p[2];
        String leaseValue=new DeviceRouteLease(gatewayId,UUID.randomUUID().toString()).encode();
        ctx.channel().attr(TENANT_ID).set(tenantId);
        ctx.channel().attr(DEVICE_ID).set(deviceId);
        ctx.channel().attr(CONNECTION_LEASE).set(leaseValue);
        registry.register(tenantId,deviceId,ctx.channel());
        touch(tenantId,deviceId,leaseValue);
        DeviceRouteLease lease=DeviceRouteLease.parse(leaseValue);
        publishLifecycle(new DeviceLifecycleEvent(
                tenantId,"ONLINE",deviceId,gatewayId,lease.connectionToken(),"AUTHENTICATED",Instant.now()));
        ctx.writeAndFlush("AUTH_ACK|"+tenantId+"|"+deviceId+"|"+gatewayId+"\n");
    }

    private void ping(ChannelHandlerContext ctx) {
        var i=identity(ctx); if(i==null)return;
        touch(i.tenantId(),i.deviceId(),i.leaseValue());
        ctx.writeAndFlush("PONG|"+i.deviceId()+"\n");
    }

    private void started(ChannelHandlerContext ctx,String[] p) {
        var i=identity(ctx); if(i==null||p.length<5)return;
        publish(new DeviceChargingEvent(i.tenantId(),"CHARGING_STARTED",i.deviceId(),p[1],Integer.parseInt(p[2]),Long.parseLong(p[3]),Integer.parseInt(p[4]),null,null,null,occurredAt(p,5)));
        ctx.writeAndFlush("CHARGING_STARTED_ACK|"+p[1]+"\n");
    }

    private void telemetry(ChannelHandlerContext ctx,String[] p) {
        var i=identity(ctx); if(i==null||p.length<6)return;
        touch(i.tenantId(),i.deviceId(),i.leaseValue());
        publish(new DeviceChargingEvent(i.tenantId(),"TELEMETRY",i.deviceId(),p[1],Integer.parseInt(p[2]),Long.parseLong(p[5]),Integer.parseInt(p[3]),Long.parseLong(p[4]),null,null,occurredAt(p,6)));
        ctx.writeAndFlush("TELEMETRY_ACK|"+p[1]+"\n");
    }

    private void stopped(ChannelHandlerContext ctx,String[] p) {
        var i=identity(ctx); if(i==null||p.length<6)return;
        publish(new DeviceChargingEvent(i.tenantId(),"CHARGING_STOPPED",i.deviceId(),p[1],Integer.parseInt(p[2]),Long.parseLong(p[3]),Integer.parseInt(p[4]),null,null,p[5],occurredAt(p,6)));
        ctx.writeAndFlush("CHARGING_STOPPED_ACK|"+p[1]+"\n");
    }

    private void alarm(ChannelHandlerContext ctx,String[] p) {
        var i=identity(ctx); if(i==null||p.length<8)return;
        touch(i.tenantId(),i.deviceId(),i.leaseValue());
        publishAlarm(new DeviceAlarmEvent(
                i.tenantId(),"RAISED",i.deviceId(),parseInteger(p[3]),p[1],p[2],
                p[4],p[5],p[6],occurredAt(p,7)));
        ctx.writeAndFlush("ALARM_ACK|"+p[1]+"\n");
    }

    private void alarmRecovered(ChannelHandlerContext ctx,String[] p) {
        var i=identity(ctx); if(i==null||p.length<4)return;
        touch(i.tenantId(),i.deviceId(),i.leaseValue());
        publishAlarm(new DeviceAlarmEvent(
                i.tenantId(),"RECOVERED",i.deviceId(),parseInteger(p[2]),p[1],"INFO",
                null,null,"recovered",occurredAt(p,3)));
        ctx.writeAndFlush("ALARM_RECOVERED_ACK|"+p[1]+"\n");
    }

    private Integer parseInteger(String value) {
        try { return Integer.valueOf(value); }
        catch (Exception ignored) { return null; }
    }

    private void publishAlarm(DeviceAlarmEvent event) {
        try {
            var envelope=new DomainEventEnvelope<>(
                    UUID.randomUUID().toString(),
                    "RAISED".equals(event.eventType()) ? "iot.device.alarm.raised" : "iot.device.alarm.recovered",
                    "1.0","Device",event.deviceId(),event.tenantId(),null,Instant.now(),"charging-iot",event);
            kafka.send("ev.device.alarm.v1",event.deviceId(),mapper.writeValueAsString(envelope));
        } catch(Exception ex) {
            throw new IllegalStateException("cannot publish device alarm event",ex);
        }
    }

    private void publishLifecycle(DeviceLifecycleEvent event) {
        String eventId="device-online:"+event.tenantId()+":"+event.deviceId()+":"+event.connectionToken();
        try {
            var envelope=new DomainEventEnvelope<>(
                    eventId,"iot.device.online","1.0","Device",event.deviceId(),
                    event.tenantId(),null,Instant.now(),"charging-iot",event);
            kafka.send("ev.device.lifecycle.v1",event.deviceId(),mapper.writeValueAsString(envelope))
                    .whenComplete((result,error)->{
                        if(error!=null) log.error("device online lifecycle publish failed: {}",event.deviceId(),error);
                    });
        } catch(Exception ex) {
            log.error("cannot serialize device online lifecycle event: {}",event.deviceId(),ex);
        }
    }

    private Instant occurredAt(String[] p,int index) {
        if(p.length<=index)return Instant.now();
        try{return Instant.ofEpochMilli(Long.parseLong(p[index]));}catch(Exception ignored){return Instant.now();}
    }

    private void publish(DeviceChargingEvent e) {
        try {
            String eventType=switch(e.eventType()) {
                case "CHARGING_STARTED" -> "iot.charging.started";
                case "TELEMETRY" -> "iot.charging.telemetry";
                case "CHARGING_STOPPED" -> "iot.charging.stopped";
                default -> "iot.charging.unknown";
            };
            var envelope=new DomainEventEnvelope<>(UUID.randomUUID().toString(),eventType,"1.0","ChargingSession",e.sessionNo(),e.tenantId(),null,Instant.now(),"charging-iot",e);
            kafka.send("ev.device.charging.v1",e.sessionNo(),mapper.writeValueAsString(envelope));
        } catch(Exception ex) { throw new IllegalStateException("cannot publish device charging event",ex); }
    }

    private Identity identity(ChannelHandlerContext ctx) {
        Long tenantId=ctx.channel().attr(TENANT_ID).get();
        String deviceId=ctx.channel().attr(DEVICE_ID).get();
        String leaseValue=ctx.channel().attr(CONNECTION_LEASE).get();
        if(tenantId==null||deviceId==null||leaseValue==null){ctx.writeAndFlush("ERR|AUTH_REQUIRED\n");return null;}
        return new Identity(tenantId,deviceId,leaseValue);
    }

    private void touch(long tenantId,String deviceId,String leaseValue) {
        redis.opsForValue().set(onlineKey(tenantId,deviceId),leaseValue,ONLINE_TTL);
        redis.opsForValue().set(routeKey(tenantId,deviceId),leaseValue,ONLINE_TTL);
        heartbeatDeadlines.touch(tenantId,deviceId,leaseValue,ONLINE_TTL);
    }

    @Override public void channelInactive(ChannelHandlerContext ctx) {
        Long tenantId=ctx.channel().attr(TENANT_ID).get();
        String deviceId=ctx.channel().attr(DEVICE_ID).get();
        String leaseValue=ctx.channel().attr(CONNECTION_LEASE).get();
        if(tenantId!=null&&deviceId!=null&&leaseValue!=null&&registry.unregister(tenantId,deviceId,ctx.channel())) {
            redis.execute(RELEASE_LEASE, List.of(onlineKey(tenantId,deviceId),routeKey(tenantId,deviceId)), leaseValue);
        }
        ctx.fireChannelInactive();
    }

    private String onlineKey(long tenantId,String deviceId){return "ev:"+tenantId+":device:online:"+deviceId;}
    private String routeKey(long tenantId,String deviceId){return "ev:"+tenantId+":device:route:"+deviceId;}
    private record Identity(long tenantId,String deviceId,String leaseValue){}
}
