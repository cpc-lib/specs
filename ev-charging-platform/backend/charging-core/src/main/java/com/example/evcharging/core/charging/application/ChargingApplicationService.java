package com.example.evcharging.core.charging.application;

import com.alibaba.csp.sentinel.annotation.SentinelResource;

import com.example.evcharging.core.asset.AssetConnectorClient;
import com.example.evcharging.core.asset.ConnectorSnapshot;
import com.example.evcharging.core.charging.domain.ChargingSessionStatus;
import com.example.evcharging.framework.context.RequestContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;


@Service
public class ChargingApplicationService {
    private final AssetConnectorClient assets;
    private final StringRedisTemplate redis;
    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper;
    private final ChargingStartTransaction startTransaction;
    private final ChargingStopTransaction stopTransaction;

    public ChargingApplicationService(AssetConnectorClient assets,StringRedisTemplate redis,JdbcTemplate jdbc,ObjectMapper mapper,ChargingStartTransaction startTransaction,ChargingStopTransaction stopTransaction){
        this.assets=assets;this.redis=redis;this.jdbc=jdbc;this.mapper=mapper;this.startTransaction=startTransaction;this.stopTransaction=stopTransaction;
    }

    /** Remote Asset/Redis checks intentionally happen before the DB write transaction. */
    public ChargingSessionView start(StartChargingRequest request){
        return startForUser(RequestContext.requireTenantId(),RequestContext.requireUserId(),request);
    }

    @SentinelResource("charging.start")
    public ChargingSessionView startForUser(long tenantId,long userId,StartChargingRequest request){
        String existing=findIdempotent(tenantId,"START_CHARGING",request.requestId());
        if(existing!=null){
            requireOwner(tenantId,userId,existing);
            return viewBySessionNo(tenantId,existing);
        }
        ConnectorSnapshot connector=assets.find(request.connectorCode(),tenantId);
        if(connector.runningStatus()!=0)throw new IllegalStateException("connector unavailable");
        if(!Boolean.TRUE.equals(redis.hasKey("ev:"+tenantId+":device:online:"+connector.deviceId())))throw new IllegalStateException("device offline");
        String sessionNo=startTransaction.create(tenantId,userId,request,connector);
        return viewBySessionNo(tenantId,sessionNo);
    }

    public ChargingSessionView stop(String sessionNo,String requestId){
        return stopForUser(RequestContext.requireTenantId(),RequestContext.requireUserId(),sessionNo,requestId);
    }

    @SentinelResource("charging.stop")
    public ChargingSessionView stopForUser(long tenantId,long userId,String sessionNo,String requestId){
        requireOwner(tenantId,userId,sessionNo);
        stopTransaction.stop(tenantId,sessionNo,requestId);
        return viewBySessionNo(tenantId,sessionNo);
    }

    public ChargingSessionView view(String sessionNo){
        long tenant=RequestContext.requireTenantId();long user=RequestContext.requireUserId();
        requireOwner(tenant,user,sessionNo);return viewBySessionNo(tenant,sessionNo);
    }

    public ChargingSessionView viewForUser(long tenantId,long userId,String sessionNo){
        requireOwner(tenantId,userId,sessionNo);return viewBySessionNo(tenantId,sessionNo);
    }

    private void requireOwner(long tenantId,long userId,String sessionNo){
        Integer count=jdbc.queryForObject(
                "SELECT COUNT(*) FROM charging_session WHERE tenant_id=? AND user_id=? AND session_no=?",
                Integer.class,tenantId,userId,sessionNo);
        if(count==null||count!=1)throw new SecurityException("charging session is not owned by current user");
    }

    private String findIdempotent(long tenantId,String operation,String requestId){
        try{return jdbc.queryForObject("SELECT resource_no FROM api_idempotency WHERE tenant_id=? AND operation_type=? AND request_id=?",String.class,tenantId,operation,requestId);}
        catch(EmptyResultDataAccessException ignored){return null;}
    }

    private ChargingSessionView viewBySessionNo(long tenantId,String sessionNo){
        var list=jdbc.query("SELECT session_no,status,connector_id,energy_wh,final_soc FROM charging_session WHERE tenant_id=? AND session_no=?",(rs,n)->{
            String status=ChargingSessionStatus.fromCode(rs.getInt("status")).name();String live=redis.opsForValue().get("ev:"+tenantId+":charging:latest:"+sessionNo);Integer soc=null;Long power=null;
            if(live!=null){try{var node=mapper.readTree(live);if(node.has("soc"))soc=node.get("soc").asInt();if(node.has("powerW"))power=node.get("powerW").asLong();}catch(Exception ignored){}}
            var orders=jdbc.query("SELECT order_no,receivable_amount_fen FROM charge_order WHERE tenant_id=? AND session_id=(SELECT id FROM charging_session WHERE tenant_id=? AND session_no=?)",(or,idx)->new Object[]{or.getString(1),or.getLong(2)},tenantId,tenantId,sessionNo);
            String orderNo=orders.isEmpty()?null:String.valueOf(orders.getFirst()[0]);Long amount=orders.isEmpty()?null:(Long)orders.getFirst()[1];
            return new ChargingSessionView(rs.getString("session_no"),status,rs.getLong("connector_id"),rs.getLong("energy_wh"),soc,power,amount,orderNo);
        },tenantId,sessionNo);
        if(list.isEmpty())throw new IllegalArgumentException("session not found");return list.getFirst();
    }
}
