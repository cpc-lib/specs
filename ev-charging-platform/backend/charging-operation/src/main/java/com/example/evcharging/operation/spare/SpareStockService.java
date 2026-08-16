package com.example.evcharging.operation.spare;

import com.example.evcharging.framework.context.RequestContext;
import com.example.evcharging.framework.id.IdGenerator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class SpareStockService {
    private final JdbcTemplate jdbc;private final IdGenerator ids;
    public SpareStockService(JdbcTemplate jdbc,IdGenerator ids){this.jdbc=jdbc;this.ids=ids;}

    @Transactional
    public long createPart(CreatePartCommand c){
        long tenant=RequestContext.requireTenantId();
        if(c.partCode()==null||c.partCode().isBlank()) throw new IllegalArgumentException("partCode required");
        if(c.minStockQty()<0) throw new IllegalArgumentException("minStockQty must be >= 0");
        long id=ids.nextId();LocalDateTime now=LocalDateTime.now();
        jdbc.update("""
            INSERT INTO operation_spare_part(
              id,tenant_id,part_code,part_name,unit,min_stock_qty,enabled,create_time,update_time
            ) VALUES (?,?,?,?,?,?,1,?,?)
            """,id,tenant,c.partCode(),c.partName(),c.unit(),c.minStockQty(),now,now);
        return id;
    }

    @Transactional
    public MovementResult receive(MovementCommand c){
        long tenant=RequestContext.requireTenantId();long user=RequestContext.requireUserId();
        MovementResult prior=existing(tenant,c.requestId());
        if(prior!=null) return prior;
        validateMovement(c);
        Part part=part(tenant,c.partCode());
        LocalDateTime now=LocalDateTime.now();

        jdbc.update("""
            INSERT INTO operation_spare_stock(id,tenant_id,warehouse_code,part_id,available_qty,version,update_time)
            VALUES (?,?,?,?,?,0,?)
            ON DUPLICATE KEY UPDATE
              available_qty=available_qty+VALUES(available_qty),version=version+1,update_time=VALUES(update_time)
            """,ids.nextId(),tenant,c.warehouseCode(),part.id(),c.quantity(),now);
        int balance=balance(tenant,c.warehouseCode(),part.id());
        return append(tenant,user,c,part.id(),"RECEIVE",c.quantity(),balance,now);
    }

    @Transactional
    public MovementResult consume(MovementCommand c){
        long tenant=RequestContext.requireTenantId();long user=RequestContext.requireUserId();
        MovementResult prior=existing(tenant,c.requestId());
        if(prior!=null) return prior;
        validateMovement(c);
        if(c.workOrderNo()==null||c.workOrderNo().isBlank()) throw new IllegalArgumentException("workOrderNo required for consume");
        Part part=part(tenant,c.partCode());
        LocalDateTime now=LocalDateTime.now();
        int updated=jdbc.update("""
            UPDATE operation_spare_stock
            SET available_qty=available_qty-?,version=version+1,update_time=?
            WHERE tenant_id=? AND warehouse_code=? AND part_id=? AND available_qty>=?
            """,c.quantity(),now,tenant,c.warehouseCode(),part.id(),c.quantity());
        if(updated!=1) throw new IllegalStateException("insufficient spare-part stock");
        int balance=balance(tenant,c.warehouseCode(),part.id());
        return append(tenant,user,c,part.id(),"CONSUME",-c.quantity(),balance,now);
    }

    private MovementResult append(long tenant,long user,MovementCommand c,long partId,String type,int delta,int balance,LocalDateTime now){
        long id=ids.nextId();String no="SPTX"+id;
        jdbc.update("""
            INSERT INTO operation_spare_stock_transaction(
              id,tenant_id,transaction_no,request_id,warehouse_code,part_id,change_type,quantity_delta,
              balance_after,work_order_no,reference_no,operator_user_id,create_time
            ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)
            """,id,tenant,no,c.requestId(),c.warehouseCode(),partId,type,delta,balance,
            c.workOrderNo(),c.referenceNo(),user,now);
        return new MovementResult(no,balance);
    }

    private MovementResult existing(long tenant,String requestId){
        if(requestId==null||requestId.isBlank()) throw new IllegalArgumentException("requestId required");
        List<MovementResult> rows=jdbc.query("""
            SELECT transaction_no,balance_after FROM operation_spare_stock_transaction
            WHERE tenant_id=? AND request_id=?
            """,(rs,n)->new MovementResult(rs.getString(1),rs.getInt(2)),tenant,requestId);
        return rows.isEmpty()?null:rows.get(0);
    }

    private Part part(long tenant,String code){
        List<Part> rows=jdbc.query("""
            SELECT id,part_code FROM operation_spare_part
            WHERE tenant_id=? AND part_code=? AND enabled=1
            """,(rs,n)->new Part(rs.getLong(1),rs.getString(2)),tenant,code);
        if(rows.isEmpty()) throw new IllegalArgumentException("spare part not found");
        return rows.get(0);
    }

    private int balance(long tenant,String warehouse,long partId){
        Integer value=jdbc.queryForObject("""
            SELECT available_qty FROM operation_spare_stock
            WHERE tenant_id=? AND warehouse_code=? AND part_id=?
            """,Integer.class,tenant,warehouse,partId);
        return value==null?0:value;
    }

    private void validateMovement(MovementCommand c){
        if(c.quantity()<=0) throw new IllegalArgumentException("quantity must be positive");
        if(c.warehouseCode()==null||c.warehouseCode().isBlank()) throw new IllegalArgumentException("warehouseCode required");
        if(c.partCode()==null||c.partCode().isBlank()) throw new IllegalArgumentException("partCode required");
    }

    public record CreatePartCommand(String partCode,String partName,String unit,int minStockQty){}
    public record MovementCommand(String requestId,String warehouseCode,String partCode,int quantity,String workOrderNo,String referenceNo){}
    public record MovementResult(String transactionNo,int balanceAfter){}
    private record Part(long id,String partCode){}
}
