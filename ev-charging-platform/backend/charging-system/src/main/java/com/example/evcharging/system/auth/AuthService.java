package com.example.evcharging.system.auth;

import com.example.evcharging.framework.context.RequestContext;
import com.example.evcharging.framework.id.IdGenerator;
import com.example.evcharging.framework.security.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;

@Service
public class AuthService {
    private final JdbcTemplate jdbc;
    private final IdGenerator ids;
    private final AccessTokenCodec codec;
    private final AuthSessionRevocationService revocations;
    private final LoginAttemptService loginAttempts;
    private final long accessMinutes;
    private final long refreshDays;

    public AuthService(
            JdbcTemplate jdbc,IdGenerator ids,ObjectMapper mapper,AuthSessionRevocationService revocations,
            LoginAttemptService loginAttempts,
            @Value("${charging.security.access-token-secret}") String secret,
            @Value("${charging.security.access-token-minutes:15}") long accessMinutes,
            @Value("${charging.security.refresh-token-days:30}") long refreshDays){
        this.jdbc=jdbc;this.ids=ids;this.codec=new AccessTokenCodec(mapper,secret);this.revocations=revocations;
        this.loginAttempts=loginAttempts;this.accessMinutes=accessMinutes;this.refreshDays=refreshDays;
    }

    public LoginResult login(LoginRequest request){
        validateLoginRequest(request);
        List<UserRow> rows=jdbc.query("""
            SELECT id,tenant_id,username,display_name,password_hash,status,failed_login_count,locked_until
            FROM sys_user WHERE tenant_id=? AND username=?
            """,(rs,n)->new UserRow(rs.getLong(1),rs.getLong(2),rs.getString(3),rs.getString(4),
                rs.getString(5),rs.getString(6),rs.getInt(7),rs.getObject(8,LocalDateTime.class)),
                request.tenantId(),request.username());

        if(rows.isEmpty()){
            loginAttempts.unknownUserFailure(request.tenantId(),request.username());
            throw new SecurityException("invalid credentials");
        }

        UserRow user=rows.get(0);
        LocalDateTime now=utcNow();
        if(!"ACTIVE".equals(user.status())) throw new SecurityException("account is disabled");
        if(user.lockedUntil()!=null&&user.lockedUntil().isAfter(now)) throw new SecurityException("account temporarily locked");

        if(!PasswordHasher.verify(request.password().toCharArray(),user.passwordHash())){
            loginAttempts.failure(user.tenantId(),user.id(),user.username());
            throw new SecurityException("invalid credentials");
        }

        loginAttempts.success(user.tenantId(),user.id(),user.username());
        AccessPrincipal principal=principal(user);
        String sessionId=UUID.randomUUID().toString();
        String refreshToken=RefreshTokenHasher.newToken();
        LocalDateTime refreshExpires=now.plusDays(refreshDays);
        jdbc.update("""
            INSERT INTO sys_auth_session(
              id,tenant_id,session_id,user_id,refresh_token_hash,status,
              created_time,last_rotated_time,expires_time
            ) VALUES (?,?,?,?,?,'ACTIVE',?,?,?)
            """,ids.nextId(),user.tenantId(),sessionId,user.id(),RefreshTokenHasher.hash(refreshToken),now,now,refreshExpires);
        TokenPair pair=issuePair(principal,sessionId,refreshToken,refreshExpires);
        securityAudit(user.tenantId(),user.id(),user.id(),"LOGIN",Map.of("sessionId",sessionId));
        return new LoginResult(pair.accessToken(),pair.accessExpiresAt(),pair.refreshToken(),pair.refreshExpiresAt(),
                pair.sessionId(),principal,user.displayName());
    }

    @Transactional
    public TokenPair refresh(RefreshRequest request){
        String hash=RefreshTokenHasher.hash(request.refreshToken());
        List<RefreshRow> rows=jdbc.query("""
            SELECT s.id,s.tenant_id,s.session_id,s.user_id,s.expires_time,s.status,
                   u.username,u.display_name,u.password_hash,u.status,u.failed_login_count,u.locked_until
            FROM sys_auth_session s JOIN sys_user u ON u.id=s.user_id
            WHERE s.refresh_token_hash=? FOR UPDATE
            """,(rs,n)->new RefreshRow(rs.getLong(1),rs.getLong(2),rs.getString(3),rs.getLong(4),
                rs.getObject(5,LocalDateTime.class),rs.getString(6),rs.getString(7),rs.getString(8),
                rs.getString(9),rs.getString(10),rs.getInt(11),rs.getObject(12,LocalDateTime.class)),hash);
        if(rows.isEmpty()) throw new SecurityException("invalid refresh token");
        RefreshRow row=rows.get(0);
        LocalDateTime now=utcNow();
        if(!"ACTIVE".equals(row.sessionStatus())||!row.expiresTime().isAfter(now))
            throw new SecurityException("refresh session expired or revoked");
        if(!"ACTIVE".equals(row.userStatus())) throw new SecurityException("account is disabled");

        String next=RefreshTokenHasher.newToken();
        int updated=jdbc.update("""
            UPDATE sys_auth_session
            SET refresh_token_hash=?,last_rotated_time=?
            WHERE id=? AND refresh_token_hash=? AND status='ACTIVE'
            """,RefreshTokenHasher.hash(next),now,row.sessionDbId(),hash);
        if(updated!=1) throw new SecurityException("refresh token already rotated");

        UserRow user=new UserRow(row.userId(),row.tenantId(),row.username(),row.displayName(),row.passwordHash(),
                row.userStatus(),row.failedLoginCount(),row.lockedUntil());
        AccessPrincipal principal=principal(user);
        securityAudit(row.tenantId(),row.userId(),row.userId(),"TOKEN_REFRESH",Map.of("sessionId",row.sessionId()));
        return issuePair(principal,row.sessionId(),next,row.expiresTime());
    }

    @Transactional
    public void changePassword(long tenantId,long userId,String currentPassword,String newPassword){
        PasswordPolicy.validate(newPassword);
        UserRow user=loadUserForUpdate(tenantId,userId);
        if(!PasswordHasher.verify(currentPassword.toCharArray(),user.passwordHash()))
            throw new SecurityException("current password is invalid");
        LocalDateTime now=utcNow();
        jdbc.update("""
            UPDATE sys_user SET password_hash=?,password_changed_time=?,failed_login_count=0,locked_until=NULL,update_time=?
            WHERE tenant_id=? AND id=?
            """,PasswordHasher.hash(newPassword.toCharArray()),now,now,tenantId,userId);
        revocations.revokeUserSessions(tenantId,userId,"PASSWORD_CHANGED");
        securityAudit(tenantId,userId,userId,"PASSWORD_CHANGED",Map.of());
    }

    public AccessPrincipal principal(UserRow user){
        Set<String> roles=new LinkedHashSet<>(),permissions=new LinkedHashSet<>();
        DataScopeType scope=DataScopeType.SELF;
        List<RoleRow> roleRows=jdbc.query("""
            SELECT r.id,r.role_code,r.data_scope_type
            FROM sys_role r JOIN sys_user_role ur ON ur.role_id=r.id
            WHERE ur.user_id=?
            """,(rs,n)->new RoleRow(rs.getLong(1),rs.getString(2),DataScopeType.valueOf(rs.getString(3))),user.id());
        for(RoleRow role:roleRows){
            roles.add(role.code());scope=merge(scope,role.scope());
            permissions.addAll(jdbc.query("""
                SELECT p.permission_code FROM sys_permission p
                JOIN sys_role_permission rp ON rp.permission_id=p.id WHERE rp.role_id=?
                """,(rs,n)->rs.getString(1),role.id()));
        }
        Set<Long> stations=new LinkedHashSet<>(jdbc.query(
                "SELECT station_id FROM sys_user_station_scope WHERE user_id=?",(rs,n)->rs.getLong(1),user.id()));
        return new AccessPrincipal(user.tenantId(),user.id(),user.username(),roles,permissions,scope,stations);
    }

    private TokenPair issuePair(AccessPrincipal principal,String sessionId,String refreshToken,LocalDateTime refreshExpires){
        Instant accessExpires=Instant.now().plus(Duration.ofMinutes(accessMinutes));
        var access=codec.issue(principal,accessExpires,sessionId);
        return new TokenPair(access.token(),access.expiresAt(),refreshToken,
                refreshExpires.toInstant(ZoneOffset.UTC),sessionId,principal);
    }

    private UserRow loadUserForUpdate(long tenant,long userId){
        List<UserRow> rows=jdbc.query("""
            SELECT id,tenant_id,username,display_name,password_hash,status,failed_login_count,locked_until
            FROM sys_user WHERE tenant_id=? AND id=? FOR UPDATE
            """,(rs,n)->new UserRow(rs.getLong(1),rs.getLong(2),rs.getString(3),rs.getString(4),rs.getString(5),
                rs.getString(6),rs.getInt(7),rs.getObject(8,LocalDateTime.class)),tenant,userId);
        if(rows.isEmpty()) throw new IllegalArgumentException("user not found");
        return rows.get(0);
    }

    private static LocalDateTime utcNow(){return LocalDateTime.ofInstant(Instant.now(),ZoneOffset.UTC);}

    private void validateLoginRequest(LoginRequest request){
        if(request==null||request.tenantId()<=0||request.username()==null||request.username().isBlank()
                ||request.password()==null||request.password().isBlank()) throw new SecurityException("invalid credentials");
    }
    private DataScopeType merge(DataScopeType a,DataScopeType b){
        List<DataScopeType> order=List.of(DataScopeType.SELF,DataScopeType.STATION,DataScopeType.TENANT,DataScopeType.ALL);
        return order.indexOf(a)>=order.indexOf(b)?a:b;
    }
    private void securityAudit(long tenant,Long actor,Long target,String type,Object detail){
        try{
            String json=new ObjectMapper().writeValueAsString(detail);
            jdbc.update("""
                INSERT INTO sys_security_audit_log(tenant_id,actor_user_id,target_user_id,event_type,detail_json,create_time)
                VALUES (?,?,?,?,?,?)
                """,tenant,actor,target,type,json,utcNow());
        }catch(Exception e){throw new IllegalStateException("cannot write security audit",e);}
    }

    public record LoginRequest(long tenantId,String username,String password){}
    public record RefreshRequest(String refreshToken){}
    public record ChangePasswordRequest(String currentPassword,String newPassword){}
    public record LoginResult(String accessToken,Instant expiresAt,String refreshToken,Instant refreshExpiresAt,
                              String sessionId,AccessPrincipal principal,String displayName){}
    public record TokenPair(String accessToken,Instant accessExpiresAt,String refreshToken,Instant refreshExpiresAt,
                            String sessionId,AccessPrincipal principal){}
    public record UserRow(long id,long tenantId,String username,String displayName,String passwordHash,String status,
                          int failedLoginCount,LocalDateTime lockedUntil){}
    private record RoleRow(long id,String code,DataScopeType scope){}
    private record RefreshRow(long sessionDbId,long tenantId,String sessionId,long userId,LocalDateTime expiresTime,
                              String sessionStatus,String username,String displayName,String passwordHash,String userStatus,
                              int failedLoginCount,LocalDateTime lockedUntil){}
}
