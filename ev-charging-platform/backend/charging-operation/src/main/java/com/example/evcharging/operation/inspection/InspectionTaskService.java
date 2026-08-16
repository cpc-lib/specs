package com.example.evcharging.operation.inspection;

import com.example.evcharging.framework.context.RequestContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InspectionTaskService {
    private final JdbcTemplate jdbc;private final ObjectMapper mapper;
    public InspectionTaskService(JdbcTemplate jdbc,ObjectMapper mapper){this.jdbc=jdbc;this.mapper=mapper;}

    @Transactional
    public void start(String taskNo){
        long tenant=RequestContext.requireTenantId();long user=RequestContext.requireUserId();
        Row row=load(tenant,taskNo);
        if(row.assigneeUserId()!=null&&row.assigneeUserId()!=user) throw new IllegalStateException("inspection task assigned to another user");
        if("IN_PROGRESS".equals(row.status())) return;
        if(!"PENDING".equals(row.status())) throw new IllegalStateException("inspection task cannot start");
        int updated=jdbc.update("""
            UPDATE operation_inspection_task
            SET status='IN_PROGRESS',assignee_user_id=COALESCE(assignee_user_id,?),started_time=?,update_time=?
            WHERE id=? AND status='PENDING'
            """,user,LocalDateTime.now(),LocalDateTime.now(),row.id());
        if(updated!=1) throw new IllegalStateException("inspection task changed concurrently");
    }

    @Transactional
    public void complete(String taskNo,JsonNode result){
        long tenant=RequestContext.requireTenantId();long user=RequestContext.requireUserId();
        Row row=load(tenant,taskNo);
        if(row.assigneeUserId()==null||row.assigneeUserId()!=user) throw new IllegalStateException("only assigned inspector may complete task");
        if("COMPLETED".equals(row.status())) return;
        if(!"IN_PROGRESS".equals(row.status())) throw new IllegalStateException("inspection task is not in progress");
        jdbc.update("""
            UPDATE operation_inspection_task
            SET status='COMPLETED',result_json=?,completed_time=?,update_time=?
            WHERE id=? AND status='IN_PROGRESS'
            """,json(result),LocalDateTime.now(),LocalDateTime.now(),row.id());
    }

    private Row load(long tenant,String no){
        List<Row> rows=jdbc.query("""
            SELECT id,status,assignee_user_id FROM operation_inspection_task
            WHERE tenant_id=? AND task_no=? FOR UPDATE
            """,(rs,n)->new Row(rs.getLong(1),rs.getString(2),(Long)rs.getObject(3)),tenant,no);
        if(rows.isEmpty()) throw new IllegalArgumentException("inspection task not found");
        return rows.get(0);
    }

    private String json(JsonNode value){
        try{return mapper.writeValueAsString(value==null?mapper.createObjectNode():value);}
        catch(Exception e){throw new IllegalArgumentException("invalid inspection result",e);}
    }

    private record Row(long id,String status,Long assigneeUserId){}
}
