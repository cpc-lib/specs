package com.example.evcharging.system.admin;

import com.example.evcharging.framework.context.RequestContext;
import com.example.evcharging.framework.id.IdGenerator;
import com.example.evcharging.framework.security.DataScopeType;
import com.example.evcharging.system.auth.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class SystemAdminService {
    private final JdbcTemplate jdbc;
    private final IdGenerator ids;
    private final AuthSessionRevocationService revocations;
    private final ObjectMapper mapper;

    public SystemAdminService(JdbcTemplate jdbc,IdGenerator ids,AuthSessionRevocationService revocations,ObjectMapper mapper){
        this.jdbc=jdbc;this.ids=ids;this.revocations=revocations;this.mapper=mapper;
    }

    @Transactional
    public long createUser(CreateUserCommand c){
        long tenant=RequestContext.requireTenantId();
        PasswordPolicy.validate(c.password());
        if(c.username()==null||c.username().isBlank()) throw new IllegalArgumentException("username required");
        long id=ids.nextId();LocalDateTime now=utcNow();
        jdbc.update("""
            INSERT INTO sys_user(id,tenant_id,username,display_name,password_hash,password_changed_time,status,
                                 failed_login_count,create_time,update_time)
            VALUES (?,?,?,?,?,?,'ACTIVE',0,?,?)
            """,id,tenant,c.username(),c.displayName(),PasswordHasher.hash(c.password().toCharArray()),now,now,now);
        replaceRolesInternal(tenant,id,c.roleCodes()==null?Set.of():c.roleCodes());
        replaceStationsInternal(id,c.stationIds()==null?Set.of():c.stationIds());
        audit(tenant,id,"USER_CREATED",Map.of("username",c.username()));
        return id;
    }

    @Transactional
    public long createPermission(CreatePermissionCommand c){
        long tenant=RequestContext.requireTenantId();
        if(c.permissionCode()==null||c.permissionCode().isBlank()) throw new IllegalArgumentException("permissionCode required");
        long id=ids.nextId();
        jdbc.update("INSERT INTO sys_permission(id,permission_code,permission_name) VALUES (?,?,?)",
                id,c.permissionCode(),c.permissionName());
        audit(tenant,null,"PERMISSION_CREATED",Map.of("permissionCode",c.permissionCode()));
        return id;
    }

    @Transactional
    public void updatePermission(String permissionCode,UpdatePermissionCommand c){
        long tenant=RequestContext.requireTenantId();
        int updated=jdbc.update("UPDATE sys_permission SET permission_name=? WHERE permission_code=?",
                c.permissionName(),permissionCode);
        if(updated!=1) throw new IllegalArgumentException("permission not found");
        List<Long> roleIds=jdbc.query("""
            SELECT DISTINCT rp.role_id FROM sys_role_permission rp
            JOIN sys_role r ON r.id=rp.role_id
            WHERE r.tenant_id=? AND rp.permission_id=(SELECT id FROM sys_permission WHERE permission_code=?)
            """,(rs,n)->rs.getLong(1),tenant,permissionCode);
        for(Long roleId:roleIds) revokeUsersOfRole(tenant,roleId,"PERMISSION_UPDATED");
        audit(tenant,null,"PERMISSION_UPDATED",Map.of("permissionCode",permissionCode));
    }

    @Transactional
    public void deletePermission(String permissionCode){
        long tenant=RequestContext.requireTenantId();
        List<Long> idsFound=jdbc.query("SELECT id FROM sys_permission WHERE permission_code=?",
                (rs,n)->rs.getLong(1),permissionCode);
        if(idsFound.isEmpty()) throw new IllegalArgumentException("permission not found");
        Long permissionId=idsFound.get(0);
        Integer assigned=jdbc.queryForObject("""
            SELECT COUNT(*) FROM sys_role_permission rp JOIN sys_role r ON r.id=rp.role_id
            WHERE r.tenant_id=? AND rp.permission_id=?
            """,Integer.class,tenant,permissionId);
        if(assigned!=null&&assigned>0) throw new IllegalStateException("permission is assigned to roles");
        jdbc.update("DELETE FROM sys_permission WHERE id=?",permissionId);
        audit(tenant,null,"PERMISSION_DELETED",Map.of("permissionCode",permissionCode));
    }

    @Transactional
    public long createRole(CreateRoleCommand c){
        long tenant=RequestContext.requireTenantId();
        DataScopeType.valueOf(c.dataScopeType());
        long id=ids.nextId();LocalDateTime now=utcNow();
        jdbc.update("""
            INSERT INTO sys_role(id,tenant_id,role_code,role_name,data_scope_type,create_time,update_time)
            VALUES (?,?,?,?,?,?,?)
            """,id,tenant,c.roleCode(),c.roleName(),c.dataScopeType(),now,now);
        replacePermissionsInternal(id,c.permissionCodes()==null?Set.of():c.permissionCodes());
        audit(tenant,null,"ROLE_CREATED",Map.of("roleCode",c.roleCode()));
        return id;
    }

    @Transactional
    public void updateRole(String roleCode,UpdateRoleCommand c){
        long tenant=RequestContext.requireTenantId();
        DataScopeType.valueOf(c.dataScopeType());
        Long roleId=roleId(tenant,roleCode);
        jdbc.update("""
            UPDATE sys_role SET role_name=?,data_scope_type=?,update_time=?
            WHERE tenant_id=? AND id=?
            """,c.roleName(),c.dataScopeType(),utcNow(),tenant,roleId);
        replacePermissionsInternal(roleId,c.permissionCodes()==null?Set.of():c.permissionCodes());
        revokeUsersOfRole(tenant,roleId,"ROLE_UPDATED");
        audit(tenant,null,"ROLE_UPDATED",Map.of("roleCode",roleCode));
    }

    @Transactional
    public void deleteRole(String roleCode){
        long tenant=RequestContext.requireTenantId();Long roleId=roleId(tenant,roleCode);
        Integer assigned=jdbc.queryForObject("SELECT COUNT(*) FROM sys_user_role WHERE role_id=?",Integer.class,roleId);
        if(assigned!=null&&assigned>0) throw new IllegalStateException("role is assigned to users");
        jdbc.update("DELETE FROM sys_role_permission WHERE role_id=?",roleId);
        jdbc.update("DELETE FROM sys_role WHERE tenant_id=? AND id=?",tenant,roleId);
        audit(tenant,null,"ROLE_DELETED",Map.of("roleCode",roleCode));
    }

    @Transactional
    public void replaceUserRoles(long userId,Set<String> roleCodes){
        long tenant=RequestContext.requireTenantId();requireUser(tenant,userId);
        replaceRolesInternal(tenant,userId,roleCodes==null?Set.of():roleCodes);
        revocations.revokeUserSessions(tenant,userId,"USER_ROLES_CHANGED");
        audit(tenant,userId,"USER_ROLES_CHANGED",Map.of("roles",roleCodes==null?Set.of():roleCodes));
    }

    @Transactional
    public void replaceStationScope(long userId,Set<Long> stationIds){
        long tenant=RequestContext.requireTenantId();requireUser(tenant,userId);
        replaceStationsInternal(userId,stationIds==null?Set.of():stationIds);
        revocations.revokeUserSessions(tenant,userId,"USER_DATA_SCOPE_CHANGED");
        audit(tenant,userId,"USER_STATION_SCOPE_CHANGED",Map.of("stationIds",stationIds==null?Set.of():stationIds));
    }

    @Transactional
    public void resetPassword(long userId,String newPassword){
        long tenant=RequestContext.requireTenantId();requireUser(tenant,userId);PasswordPolicy.validate(newPassword);
        LocalDateTime now=utcNow();
        jdbc.update("""
            UPDATE sys_user SET password_hash=?,password_changed_time=?,failed_login_count=0,locked_until=NULL,update_time=?
            WHERE tenant_id=? AND id=?
            """,PasswordHasher.hash(newPassword.toCharArray()),now,now,tenant,userId);
        revocations.revokeUserSessions(tenant,userId,"ADMIN_PASSWORD_RESET");
        audit(tenant,userId,"ADMIN_PASSWORD_RESET",Map.of());
    }

    @Transactional
    public void setStatus(long userId,String status){
        long tenant=RequestContext.requireTenantId();requireUser(tenant,userId);
        String target=status==null?"":status.toUpperCase(Locale.ROOT);
        if(!Set.of("ACTIVE","DISABLED").contains(target)) throw new IllegalArgumentException("unsupported user status");
        jdbc.update("UPDATE sys_user SET status=?,update_time=? WHERE tenant_id=? AND id=?",
                target,utcNow(),tenant,userId);
        if("DISABLED".equals(target)) revocations.revokeUserSessions(tenant,userId,"ACCOUNT_DISABLED");
        audit(tenant,userId,"USER_STATUS_CHANGED",Map.of("status",target));
    }

    public List<UserView> users(){
        long tenant=RequestContext.requireTenantId();
        return jdbc.query("""
            SELECT u.id,u.username,u.display_name,u.status,u.locked_until,u.create_time,
                   GROUP_CONCAT(DISTINCT r.role_code ORDER BY r.role_code SEPARATOR ',')
            FROM sys_user u
            LEFT JOIN sys_user_role ur ON ur.user_id=u.id
            LEFT JOIN sys_role r ON r.id=ur.role_id
            WHERE u.tenant_id=?
            GROUP BY u.id,u.username,u.display_name,u.status,u.locked_until,u.create_time
            ORDER BY u.id
            """,(rs,n)->new UserView(rs.getLong(1),rs.getString(2),rs.getString(3),rs.getString(4),
                String.valueOf(rs.getObject(5)),String.valueOf(rs.getObject(6)),
                csv(rs.getString(7))),tenant);
    }

    public List<RoleView> roles(){
        long tenant=RequestContext.requireTenantId();
        List<RoleBase> bases=jdbc.query("""
            SELECT id,role_code,role_name,data_scope_type FROM sys_role WHERE tenant_id=? ORDER BY id
            """,(rs,n)->new RoleBase(rs.getLong(1),rs.getString(2),rs.getString(3),rs.getString(4)),tenant);
        return bases.stream().map(r->new RoleView(r.id(),r.roleCode(),r.roleName(),r.dataScopeType(),
                Set.copyOf(jdbc.query("""
                    SELECT p.permission_code FROM sys_permission p JOIN sys_role_permission rp ON rp.permission_id=p.id
                    WHERE rp.role_id=? ORDER BY p.permission_code
                    """,(rs,n)->rs.getString(1),r.id())))).toList();
    }

    public List<PermissionView> permissions(){
        return jdbc.query("SELECT id,permission_code,permission_name FROM sys_permission ORDER BY permission_code",
                (rs,n)->new PermissionView(rs.getLong(1),rs.getString(2),rs.getString(3)));
    }

    public Set<Long> stationScope(long userId){
        long tenant=RequestContext.requireTenantId();requireUser(tenant,userId);
        return new LinkedHashSet<>(jdbc.query("SELECT station_id FROM sys_user_station_scope WHERE user_id=? ORDER BY station_id",
                (rs,n)->rs.getLong(1),userId));
    }

    private void replaceRolesInternal(long tenant,long userId,Set<String> roleCodes){
        jdbc.update("DELETE FROM sys_user_role WHERE user_id=?",userId);
        for(String code:roleCodes){
            Long roleId=roleId(tenant,code);
            jdbc.update("INSERT INTO sys_user_role(user_id,role_id) VALUES (?,?)",userId,roleId);
        }
    }
    private void replaceStationsInternal(long userId,Set<Long> stationIds){
        jdbc.update("DELETE FROM sys_user_station_scope WHERE user_id=?",userId);
        for(Long stationId:stationIds) if(stationId!=null&&stationId>0)
            jdbc.update("INSERT INTO sys_user_station_scope(user_id,station_id) VALUES (?,?)",userId,stationId);
    }
    private void replacePermissionsInternal(long roleId,Set<String> permissionCodes){
        jdbc.update("DELETE FROM sys_role_permission WHERE role_id=?",roleId);
        for(String code:permissionCodes){
            List<Long> ids=jdbc.query("SELECT id FROM sys_permission WHERE permission_code=?",(rs,n)->rs.getLong(1),code);
            if(ids.isEmpty()) throw new IllegalArgumentException("permission not found: "+code);
            jdbc.update("INSERT INTO sys_role_permission(role_id,permission_id) VALUES (?,?)",roleId,ids.get(0));
        }
    }
    private Long roleId(long tenant,String code){
        List<Long> rows=jdbc.query("SELECT id FROM sys_role WHERE tenant_id=? AND role_code=?",
                (rs,n)->rs.getLong(1),tenant,code);
        if(rows.isEmpty()) throw new IllegalArgumentException("role not found: "+code);
        return rows.get(0);
    }
    private void requireUser(long tenant,long userId){
        Integer c=jdbc.queryForObject("SELECT COUNT(*) FROM sys_user WHERE tenant_id=? AND id=?",Integer.class,tenant,userId);
        if(c==null||c!=1) throw new IllegalArgumentException("user not found");
    }
    private void revokeUsersOfRole(long tenant,long roleId,String reason){
        List<Long> users=jdbc.query("""
            SELECT u.id FROM sys_user u JOIN sys_user_role ur ON ur.user_id=u.id
            WHERE u.tenant_id=? AND ur.role_id=?
            """,(rs,n)->rs.getLong(1),tenant,roleId);
        for(Long user:users) revocations.revokeUserSessions(tenant,user,reason);
    }
    private void audit(long tenant,Long target,String type,Object detail){
        try{
            jdbc.update("""
                INSERT INTO sys_security_audit_log(tenant_id,actor_user_id,target_user_id,event_type,detail_json,create_time)
                VALUES (?,?,?,?,?,?)
                """,tenant,RequestContext.currentUserId().isPresent()?RequestContext.currentUserId().getAsLong():null,
                target,type,mapper.writeValueAsString(detail),utcNow());
        }catch(Exception e){throw new IllegalStateException("cannot audit system change",e);}
    }
    private static LocalDateTime utcNow(){return LocalDateTime.ofInstant(java.time.Instant.now(),java.time.ZoneOffset.UTC);}
    private Set<String> csv(String value){
        if(value==null||value.isBlank())return Set.of();
        return new LinkedHashSet<>(Arrays.asList(value.split(",")));
    }

    public record CreateUserCommand(String username,String displayName,String password,Set<String> roleCodes,Set<Long> stationIds){}
    public record CreateRoleCommand(String roleCode,String roleName,String dataScopeType,Set<String> permissionCodes){}
    public record UpdateRoleCommand(String roleName,String dataScopeType,Set<String> permissionCodes){}
    public record CreatePermissionCommand(String permissionCode,String permissionName){}
    public record UpdatePermissionCommand(String permissionName){}
    public record UserView(long id,String username,String displayName,String status,String lockedUntil,String createTime,Set<String> roles){}
    public record RoleView(long id,String roleCode,String roleName,String dataScopeType,Set<String> permissions){}
    public record PermissionView(long id,String permissionCode,String permissionName){}
    private record RoleBase(long id,String roleCode,String roleName,String dataScopeType){}
}
