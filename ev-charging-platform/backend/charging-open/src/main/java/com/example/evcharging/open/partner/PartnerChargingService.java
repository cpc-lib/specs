package com.example.evcharging.open.partner;

import com.example.evcharging.framework.id.IdGenerator;
import com.example.evcharging.open.integration.CorePartnerClient;
import com.example.evcharging.open.integration.AssetOpenClient;
import com.example.evcharging.open.security.*;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;

@Service
public class PartnerChargingService {
    private final JdbcTemplate jdbc;private final IdGenerator ids;private final PartnerIdentityService identities;
    private final CorePartnerClient core;private final AssetOpenClient assets;

    public PartnerChargingService(JdbcTemplate jdbc,IdGenerator ids,PartnerIdentityService identities,
                                  CorePartnerClient core,AssetOpenClient assets){
        this.jdbc=jdbc;this.ids=ids;this.identities=identities;this.core=core;this.assets=assets;
    }

    public StartResult start(StartRequest request){
        PartnerScopeGuard.require("charging:write");
        var p=PartnerContext.require();
        if(request.requestId()==null||request.requestId().isBlank())throw new IllegalArgumentException("requestId required");
        List<StartResult> prior=jdbc.query("""
            SELECT session_no,external_user_id,connector_code FROM open_partner_charging_ref
            WHERE partner_id=? AND partner_request_id=?
            """,(rs,n)->new StartResult(rs.getString(1),rs.getString(2),rs.getString(3)),p.partnerId(),request.requestId());
        if(!prior.isEmpty())return prior.getFirst();

        var connector=assets.connector(request.connectorCode());
        PartnerScopeGuard.requireStation(connector.stationId());
        long localUser=identities.localUserId(request.externalUserId());
        String internalRequest="partner:"+p.partnerId()+":"+request.requestId();
        CorePartnerClient.SessionView session=core.start(new CorePartnerClient.StartCommand(localUser,internalRequest,request.connectorCode()));
        try{
            jdbc.update("""
                INSERT INTO open_partner_charging_ref(
                  id,tenant_id,partner_id,partner_request_id,external_user_id,session_no,connector_code,create_time
                ) VALUES (?,?,?,?,?,?,?,?)
                """,ids.nextId(),p.tenantId(),p.partnerId(),request.requestId(),request.externalUserId(),
                session.sessionNo(),request.connectorCode(),utcNow());
        }catch(DuplicateKeyException duplicate){
            // Core start is already idempotent. Return the authoritative persisted partner mapping if another thread won.
        }
        return new StartResult(session.sessionNo(),request.externalUserId(),request.connectorCode());
    }

    public CorePartnerClient.SessionView stop(String sessionNo,StopRequest request){
        PartnerScopeGuard.require("charging:write");
        var p=PartnerContext.require();
        List<String> refs=jdbc.query("""
            SELECT external_user_id FROM open_partner_charging_ref
            WHERE partner_id=? AND session_no=?
            """,(rs,n)->rs.getString(1),p.partnerId(),sessionNo);
        if(refs.isEmpty())throw new IllegalArgumentException("partner charging session not found");
        long localUser=identities.localUserId(refs.getFirst());
        return core.stop(sessionNo,new CorePartnerClient.StopCommand(
                localUser,"partner:"+p.partnerId()+":stop:"+request.requestId()));
    }

    public CorePartnerClient.OrderSnapshot order(String orderNo){
        PartnerScopeGuard.require("order:read");
        CorePartnerClient.OrderSnapshot order=core.order(orderNo);
        if(!identities.belongsToPartner(order.localUserId()))throw new SecurityException("order does not belong to current partner");
        PartnerScopeGuard.requireStation(order.stationId());
        return order;
    }

    private static LocalDateTime utcNow(){return LocalDateTime.ofInstant(Instant.now(),ZoneOffset.UTC);}
    public record StartRequest(String requestId,String externalUserId,String connectorCode){}
    public record StopRequest(String requestId){}
    public record StartResult(String sessionNo,String externalUserId,String connectorCode){}
}
