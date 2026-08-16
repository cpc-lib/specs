package com.example.evcharging.open.admin;

import com.example.evcharging.framework.context.RequestContext;
import com.example.evcharging.open.security.SecretCipher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class OpenSecretRewrapService {
    private final JdbcTemplate jdbc;private final SecretCipher cipher;
    public OpenSecretRewrapService(JdbcTemplate jdbc,SecretCipher cipher){this.jdbc=jdbc;this.cipher=cipher;}

    @Transactional
    public Result rewrapTenantSecrets(){
        long tenant=RequestContext.requireTenantId();int changed=0;
        changed+=rewrapColumn("open_partner_app","secret_ciphertext",tenant);
        changed+=rewrapColumn("open_partner_app","callback_secret_ciphertext",tenant);
        changed+=rewrapColumn("open_regulatory_platform","credential_secret_ciphertext",tenant);
        return new Result(cipher.activeKeyId(),changed);
    }

    private int rewrapColumn(String table,String column,long tenant){
        if(!Set.of("open_partner_app","open_regulatory_platform").contains(table)
                ||!Set.of("secret_ciphertext","callback_secret_ciphertext","credential_secret_ciphertext").contains(column))
            throw new IllegalArgumentException("unsupported rewrap column");
        List<Row> rows=jdbc.query("SELECT id,"+column+" FROM "+table+" WHERE tenant_id=? AND "+column+" IS NOT NULL",
                (rs,n)->new Row(rs.getLong(1),rs.getString(2)),tenant);
        int changed=0;
        for(Row row:rows){
            if(cipher.usesActiveKey(row.ciphertext()))continue;
            String next=cipher.rewrap(row.ciphertext());
            changed+=jdbc.update("UPDATE "+table+" SET "+column+"=?,update_time=UTC_TIMESTAMP(3) WHERE tenant_id=? AND id=?",
                    next,tenant,row.id());
        }
        return changed;
    }

    private record Row(long id,String ciphertext){}
    public record Result(String activeKeyId,int rewrappedSecrets){}
}
