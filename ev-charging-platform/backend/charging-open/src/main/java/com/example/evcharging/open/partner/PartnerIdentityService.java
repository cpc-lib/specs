package com.example.evcharging.open.partner;

import com.example.evcharging.framework.id.IdGenerator;
import com.example.evcharging.open.security.PartnerContext;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.List;

@Service
public class PartnerIdentityService {
    private final JdbcTemplate jdbc;private final IdGenerator ids;
    public PartnerIdentityService(JdbcTemplate jdbc,IdGenerator ids){this.jdbc=jdbc;this.ids=ids;}

    @Transactional
    public long localUserId(String externalUserId){
        if(externalUserId==null||externalUserId.isBlank()||externalUserId.length()>128)
            throw new IllegalArgumentException("externalUserId required");
        var p=PartnerContext.require();
        List<Long> existing=jdbc.query("""
            SELECT local_user_id FROM open_partner_user_mapping
            WHERE partner_id=? AND external_user_id=?
            """,(rs,n)->rs.getLong(1),p.partnerId(),externalUserId);
        if(!existing.isEmpty())return existing.getFirst();
        long id=ids.nextId();long localUser=ids.nextId();
        try{
            jdbc.update("""
                INSERT INTO open_partner_user_mapping(
                  id,tenant_id,partner_id,external_user_id,local_user_id,create_time
                ) VALUES (?,?,?,?,?,?)
                """,id,p.tenantId(),p.partnerId(),externalUserId,localUser,utcNow());
            return localUser;
        }catch(DuplicateKeyException race){
            return jdbc.queryForObject("""
                SELECT local_user_id FROM open_partner_user_mapping
                WHERE partner_id=? AND external_user_id=?
                """,Long.class,p.partnerId(),externalUserId);
        }
    }

    public boolean belongsToPartner(long localUserId){
        var p=PartnerContext.require();
        Integer count=jdbc.queryForObject("""
            SELECT COUNT(*) FROM open_partner_user_mapping
            WHERE partner_id=? AND local_user_id=?
            """,Integer.class,p.partnerId(),localUserId);
        return count!=null&&count==1;
    }

    private static LocalDateTime utcNow(){return LocalDateTime.ofInstant(Instant.now(),ZoneOffset.UTC);}
}
