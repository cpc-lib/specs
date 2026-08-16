package com.example.evcharging.system.bootstrap;

import com.example.evcharging.system.auth.PasswordHasher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Component
@ConditionalOnProperty(name="charging.security.bootstrap-demo-users",havingValue="true")
public class ProductIamBootstrap implements CommandLineRunner {
    private final JdbcTemplate jdbc;
    public ProductIamBootstrap(JdbcTemplate jdbc){this.jdbc=jdbc;}

    @Override @Transactional
    public void run(String... args){
        Integer count=jdbc.queryForObject("SELECT COUNT(*) FROM sys_user",Integer.class);
        if(count!=null&&count>0) return;

        long tenant=1L;
        createPermission(1,"*","All permissions");
        createPermission(2,"system:manage","System management");
        createPermission(3,"asset:read","Asset read");
        createPermission(4,"operation:read","Operation read");
        createPermission(5,"open:manage","OpenAPI partner management");
        createPermission(6,"regulatory:manage","Regulatory integration management");

        createRole(101,tenant,"ADMIN","Administrator","ALL");
        createRole(102,tenant,"MERCHANT","Merchant Operator","TENANT");
        createRole(103,tenant,"TECHNICIAN","Field Technician","STATION");
        createRole(104,tenant,"MEMBER","EV Driver","SELF");
        createRole(105,tenant,"MERCHANT_STATION","Merchant Station Scope","STATION");

        rolePermission(101,1); rolePermission(102,3); rolePermission(102,4); rolePermission(103,4);
        rolePermission(105,3); rolePermission(105,4);

        createUser(10001,tenant,"admin","Platform Admin","admin123456",101);
        createUser(10005,tenant,"admin2","Finance Approver","admin2123456",101);
        createUser(10002,tenant,"merchant","Merchant Demo","merchant123456",102);
        createUser(10003,tenant,"technician","Technician Demo","tech123456",103);
        createUser(10004,tenant,"driver","Driver Demo","driver123456",104);
    }

    private void createPermission(long id,String code,String name){
        jdbc.update("INSERT INTO sys_permission(id,permission_code,permission_name) VALUES (?,?,?)",id,code,name);
    }
    private void createRole(long id,long tenant,String code,String name,String scope){
        jdbc.update("""
            INSERT INTO sys_role(id,tenant_id,role_code,role_name,data_scope_type,create_time)
            VALUES (?,?,?,?,?,?)
            """,id,tenant,code,name,scope,LocalDateTime.now());
    }
    private void rolePermission(long role,long permission){
        jdbc.update("INSERT INTO sys_role_permission(role_id,permission_id) VALUES (?,?)",role,permission);
    }
    private void createUser(long id,long tenant,String username,String display,String password,long role){
        LocalDateTime now=LocalDateTime.now();
        jdbc.update("""
            INSERT INTO sys_user(id,tenant_id,username,display_name,password_hash,status,create_time,update_time)
            VALUES (?,?,?,?,?,'ACTIVE',?,?)
            """,id,tenant,username,display,PasswordHasher.hash(password.toCharArray()),now,now);
        jdbc.update("INSERT INTO sys_user_role(user_id,role_id) VALUES (?,?)",id,role);
    }
}
