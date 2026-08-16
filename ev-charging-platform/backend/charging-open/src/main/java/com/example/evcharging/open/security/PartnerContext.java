package com.example.evcharging.open.security;

import java.util.Set;

public final class PartnerContext {
    private static final ThreadLocal<Principal> LOCAL=new ThreadLocal<>();
    private PartnerContext(){}

    public static void set(Principal principal){LOCAL.set(principal);}
    public static Principal require(){
        Principal p=LOCAL.get();
        if(p==null)throw new SecurityException("partner context missing");
        return p;
    }
    public static Principal current(){return LOCAL.get();}
    public static void clear(){LOCAL.remove();}

    public record Principal(
            long tenantId,long partnerId,String partnerCode,String appKey,
            Set<String> scopes,String dataScopeType,Set<Long> stationIds,
            int rateLimitPerMinute){
        public Principal{
            scopes=scopes==null?Set.of():Set.copyOf(scopes);
            stationIds=stationIds==null?Set.of():Set.copyOf(stationIds);
        }
        public boolean hasScope(String scope){return scopes.contains("*")||scopes.contains(scope);}
        public boolean stationAllowed(long stationId){
            if("ALL".equalsIgnoreCase(dataScopeType))return true;
            return "STATION".equalsIgnoreCase(dataScopeType)&&stationIds.contains(stationId);
        }
    }
}
