package com.example.evcharging.open.admin;

import com.example.evcharging.framework.context.RequestContext;
import com.example.evcharging.framework.id.IdGenerator;
import com.example.evcharging.open.security.SecretCipher;
import com.example.evcharging.open.security.OutboundUrlPolicy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.*;
import java.util.*;
import java.util.Base64;

@Service
public class PartnerManagementService {
    private static final SecureRandom RANDOM=new SecureRandom();
    private final JdbcTemplate jdbc;private final IdGenerator ids;private final SecretCipher cipher;private final OutboundUrlPolicy outbound;

    public PartnerManagementService(JdbcTemplate jdbc,IdGenerator ids,SecretCipher cipher,OutboundUrlPolicy outbound){
        this.jdbc=jdbc;this.ids=ids;this.cipher=cipher;this.outbound=outbound;
    }

    @Transactional
    public CreatedPartner create(CreatePartnerCommand c){
        long tenant=RequestContext.requireTenantId();
        validateScope(c.dataScopeType(),c.stationIds());
        if(c.callbackUrl()!=null&&!c.callbackUrl().isBlank())outbound.requireAllowed(c.callbackUrl());
        if(c.rateLimitPerMinute()<=0||c.rateLimitPerMinute()>10000)throw new IllegalArgumentException("invalid rate limit");
        long id=ids.nextId();String appKey="evp_"+random(18),secret=random(32);
        String callbackSecret=c.callbackUrl()==null||c.callbackUrl().isBlank()?null:random(32);
        LocalDateTime now=utcNow();
        jdbc.update("""
            INSERT INTO open_partner_app(
              id,tenant_id,partner_code,partner_name,app_key,secret_ciphertext,status,data_scope_type,
              rate_limit_per_minute,callback_url,callback_secret_ciphertext,create_time,update_time
            ) VALUES (?,?,?,?,?,?,'ACTIVE',?,?,?,?,?,?)
            """,id,tenant,c.partnerCode(),c.partnerName(),appKey,cipher.encrypt(secret),c.dataScopeType(),
                c.rateLimitPerMinute(),blankToNull(c.callbackUrl()),cipher.encrypt(callbackSecret),now,now);
        replaceScopes(id,c.scopes());replaceStations(id,c.stationIds());
        return new CreatedPartner(id,appKey,secret,callbackSecret);
    }

    @Transactional
    public RotatedSecret rotateSecret(long partnerId){
        long tenant=RequestContext.requireTenantId();requirePartner(tenant,partnerId);
        String secret=random(32);
        jdbc.update("UPDATE open_partner_app SET secret_ciphertext=?,update_time=? WHERE tenant_id=? AND id=?",
                cipher.encrypt(secret),utcNow(),tenant,partnerId);
        return new RotatedSecret(secret);
    }

    @Transactional
    public RotatedSecret rotateCallbackSecret(long partnerId){
        long tenant=RequestContext.requireTenantId();requirePartner(tenant,partnerId);
        String secret=random(32);
        jdbc.update("UPDATE open_partner_app SET callback_secret_ciphertext=?,update_time=? WHERE tenant_id=? AND id=?",
                cipher.encrypt(secret),utcNow(),tenant,partnerId);
        return new RotatedSecret(secret);
    }

    @Transactional
    public void updateAccess(long partnerId,UpdateAccessCommand c){
        long tenant=RequestContext.requireTenantId();requirePartner(tenant,partnerId);
        validateScope(c.dataScopeType(),c.stationIds());
        if(!Set.of("ACTIVE","DISABLED").contains(c.status()))throw new IllegalArgumentException("partner status must be ACTIVE or DISABLED");
        if(c.rateLimitPerMinute()<=0||c.rateLimitPerMinute()>10000)throw new IllegalArgumentException("invalid rate limit");
        if(c.callbackUrl()!=null&&!c.callbackUrl().isBlank())outbound.requireAllowed(c.callbackUrl());
        jdbc.update("""
            UPDATE open_partner_app SET data_scope_type=?,rate_limit_per_minute=?,callback_url=?,status=?,update_time=?
            WHERE tenant_id=? AND id=?
            """,c.dataScopeType(),c.rateLimitPerMinute(),blankToNull(c.callbackUrl()),c.status(),utcNow(),tenant,partnerId);
        replaceScopes(partnerId,c.scopes());replaceStations(partnerId,c.stationIds());
    }

    public List<PartnerView> list(){
        long tenant=RequestContext.requireTenantId();
        return jdbc.query("""
            SELECT id,partner_code,partner_name,app_key,status,data_scope_type,rate_limit_per_minute,callback_url,create_time
            FROM open_partner_app WHERE tenant_id=? ORDER BY id DESC
            """,(rs,n)->new PartnerView(rs.getLong(1),rs.getString(2),rs.getString(3),rs.getString(4),
                rs.getString(5),rs.getString(6),rs.getInt(7),rs.getString(8),String.valueOf(rs.getObject(9))),tenant);
    }

    public PartnerDetail detail(long partnerId){
        long tenant=RequestContext.requireTenantId();requirePartner(tenant,partnerId);
        PartnerView p=jdbc.queryForObject("""
            SELECT id,partner_code,partner_name,app_key,status,data_scope_type,rate_limit_per_minute,callback_url,create_time
            FROM open_partner_app WHERE tenant_id=? AND id=?
            """,(rs,n)->new PartnerView(rs.getLong(1),rs.getString(2),rs.getString(3),rs.getString(4),
                rs.getString(5),rs.getString(6),rs.getInt(7),rs.getString(8),String.valueOf(rs.getObject(9))),tenant,partnerId);
        Set<String> scopes=new LinkedHashSet<>(jdbc.query("SELECT scope_code FROM open_partner_scope WHERE partner_id=?",
                (rs,n)->rs.getString(1),partnerId));
        Set<Long> stations=new LinkedHashSet<>(jdbc.query("SELECT station_id FROM open_partner_station_scope WHERE partner_id=?",
                (rs,n)->rs.getLong(1),partnerId));
        return new PartnerDetail(p,scopes,stations);
    }

    private void replaceScopes(long partnerId,Set<String> scopes){
        jdbc.update("DELETE FROM open_partner_scope WHERE partner_id=?",partnerId);
        if(scopes!=null)for(String scope:scopes)if(scope!=null&&!scope.isBlank())
            jdbc.update("INSERT INTO open_partner_scope(partner_id,scope_code) VALUES (?,?)",partnerId,scope.trim());
    }
    private void replaceStations(long partnerId,Set<Long> stations){
        jdbc.update("DELETE FROM open_partner_station_scope WHERE partner_id=?",partnerId);
        if(stations!=null)for(Long station:stations)if(station!=null&&station>0)
            jdbc.update("INSERT INTO open_partner_station_scope(partner_id,station_id) VALUES (?,?)",partnerId,station);
    }
    private void validateScope(String type,Set<Long> stations){
        if(!Set.of("ALL","STATION").contains(type))throw new IllegalArgumentException("partner dataScopeType must be ALL or STATION");
        if("STATION".equals(type)&&(stations==null||stations.isEmpty()))throw new IllegalArgumentException("STATION scope requires stationIds");
    }
    private void requirePartner(long tenant,long id){
        Integer c=jdbc.queryForObject("SELECT COUNT(*) FROM open_partner_app WHERE tenant_id=? AND id=?",Integer.class,tenant,id);
        if(c==null||c!=1)throw new IllegalArgumentException("partner not found");
    }
    private static String random(int bytes){byte[] b=new byte[bytes];RANDOM.nextBytes(b);return Base64.getUrlEncoder().withoutPadding().encodeToString(b);}
    private static String blankToNull(String v){return v==null||v.isBlank()?null:v.trim();}
    private static LocalDateTime utcNow(){return LocalDateTime.ofInstant(Instant.now(),ZoneOffset.UTC);}

    public record CreatePartnerCommand(String partnerCode,String partnerName,String dataScopeType,int rateLimitPerMinute,
                                       Set<String> scopes,Set<Long> stationIds,String callbackUrl){}
    public record UpdateAccessCommand(String dataScopeType,int rateLimitPerMinute,Set<String> scopes,Set<Long> stationIds,
                                      String callbackUrl,String status){}
    public record CreatedPartner(long partnerId,String appKey,String appSecret,String callbackSecret){}
    public record RotatedSecret(String secret){}
    public record PartnerView(long partnerId,String partnerCode,String partnerName,String appKey,String status,
                              String dataScopeType,int rateLimitPerMinute,String callbackUrl,String createTime){}
    public record PartnerDetail(PartnerView partner,Set<String> scopes,Set<Long> stationIds){}
}
