package com.example.evcharging.open.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.*;

@Component
public class OutboundUrlPolicy {
    private final boolean production;
    private final Set<String> allowedHosts;

    public OutboundUrlPolicy(
            @Value("${APP_ENV:dev}") String appEnv,
            @Value("${charging.open.allowed-outbound-hosts:${OPENAPI_ALLOWED_OUTBOUND_HOSTS:}}") String hosts){
        this.production="prod".equalsIgnoreCase(appEnv)||"production".equalsIgnoreCase(appEnv);
        Set<String> parsed=new LinkedHashSet<>();
        if(hosts!=null)for(String h:hosts.split(","))if(!h.isBlank())parsed.add(h.trim().toLowerCase(Locale.ROOT));
        this.allowedHosts=Set.copyOf(parsed);
        if(production&&allowedHosts.isEmpty())
            throw new IllegalStateException("production OPENAPI_ALLOWED_OUTBOUND_HOSTS must be configured");
    }

    public URI requireAllowed(String url){
        try{
            URI uri=URI.create(url);
            String scheme=uri.getScheme(),host=uri.getHost();
            if(host==null||scheme==null||uri.getUserInfo()!=null)throw new IllegalArgumentException("invalid outbound URL");
            if(!Set.of("http","https").contains(scheme.toLowerCase(Locale.ROOT)))throw new IllegalArgumentException("unsupported outbound URL scheme");
            if(production&&!"https".equalsIgnoreCase(scheme))throw new IllegalArgumentException("production outbound URL must use https");
            String normalized=host.toLowerCase(Locale.ROOT);
            if(production&&!allowedHosts.contains(normalized))throw new SecurityException("outbound host is not allowlisted");
            if(production&&isLiteralPrivate(normalized))throw new SecurityException("private/loopback literal outbound address denied");
            return uri;
        }catch(SecurityException|IllegalArgumentException e){throw e;}
        catch(Exception e){throw new IllegalArgumentException("invalid outbound URL",e);}
    }

    private boolean isLiteralPrivate(String host){
        if("localhost".equals(host)||"::1".equals(host)||host.startsWith("127."))return true;
        String[] p=host.split("\\.");
        if(p.length!=4)return false;
        try{
            int a=Integer.parseInt(p[0]),b=Integer.parseInt(p[1]);
            return a==10||a==127||(a==192&&b==168)||(a==172&&b>=16&&b<=31)||(a==169&&b==254);
        }catch(NumberFormatException e){return false;}
    }
}
