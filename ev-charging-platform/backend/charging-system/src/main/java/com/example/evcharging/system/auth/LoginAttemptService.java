package com.example.evcharging.system.auth;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;

import java.time.*;
import java.util.List;

@Service
public class LoginAttemptService {
    private final JdbcTemplate jdbc;
    public LoginAttemptService(JdbcTemplate jdbc){this.jdbc=jdbc;}

    @Transactional(propagation=Propagation.REQUIRES_NEW)
    public void failure(long tenantId,long userId,String username){
        List<Row> rows=jdbc.query("""
            SELECT failed_login_count FROM sys_user WHERE tenant_id=? AND id=? FOR UPDATE
            """,(rs,n)->new Row(rs.getInt(1)),tenantId,userId);
        if(rows.isEmpty())return;
        int failed=rows.get(0).failed()+1;
        LocalDateTime now=utcNow();
        LocalDateTime locked=failed>=5?now.plusMinutes(15):null;
        jdbc.update("UPDATE sys_user SET failed_login_count=?,locked_until=?,update_time=? WHERE tenant_id=? AND id=?",
                failed,locked,now,tenantId,userId);
        jdbc.update("""
            INSERT INTO sys_login_log(tenant_id,user_id,username,success,failure_reason,login_time)
            VALUES (?,?,?,0,'INVALID_CREDENTIALS',?)
            """,tenantId,userId,username,now);
    }

    @Transactional(propagation=Propagation.REQUIRES_NEW)
    public void success(long tenantId,long userId,String username){
        LocalDateTime now=utcNow();
        jdbc.update("UPDATE sys_user SET failed_login_count=0,locked_until=NULL,update_time=? WHERE tenant_id=? AND id=?",
                now,tenantId,userId);
        jdbc.update("""
            INSERT INTO sys_login_log(tenant_id,user_id,username,success,failure_reason,login_time)
            VALUES (?,?,?,1,NULL,?)
            """,tenantId,userId,username,now);
    }

    @Transactional(propagation=Propagation.REQUIRES_NEW)
    public void unknownUserFailure(long tenantId,String username){
        jdbc.update("""
            INSERT INTO sys_login_log(tenant_id,user_id,username,success,failure_reason,login_time)
            VALUES (?,NULL,?,0,'INVALID_CREDENTIALS',?)
            """,tenantId,username,utcNow());
    }

    private static LocalDateTime utcNow(){return LocalDateTime.ofInstant(Instant.now(),ZoneOffset.UTC);}
    private record Row(int failed){}
}
