package com.example.evcharging.open.security;

public final class PartnerScopeGuard {
    private PartnerScopeGuard(){}
    public static void require(String scope){
        if(!PartnerContext.require().hasScope(scope))
            throw new SecurityException("partner scope denied: "+scope);
    }
    public static void requireStation(long stationId){
        if(!PartnerContext.require().stationAllowed(stationId))
            throw new SecurityException("station outside partner scope");
    }
}
