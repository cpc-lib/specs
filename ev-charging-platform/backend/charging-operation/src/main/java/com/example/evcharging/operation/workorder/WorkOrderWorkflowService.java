package com.example.evcharging.operation.workorder;

import com.example.evcharging.framework.context.RequestContext;
import org.flowable.task.api.Task;
import org.flowable.engine.TaskService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class WorkOrderWorkflowService {
    private final JdbcTemplate jdbc;
    private final TaskService taskService;
    private final WorkOrderCreationService events;

    public WorkOrderWorkflowService(JdbcTemplate jdbc,TaskService taskService,WorkOrderCreationService events){
        this.jdbc=jdbc;this.taskService=taskService;this.events=events;
    }

    @Transactional
    public void assign(String workOrderNo,long assigneeUserId){
        if(assigneeUserId<=0) throw new IllegalArgumentException("assigneeUserId required");
        long tenant=RequestContext.requireTenantId();
        long operator=RequestContext.requireUserId();
        WorkOrderRow row=loadForUpdate(tenant,workOrderNo);
        if(row.state()!=WorkOrderState.PENDING_ASSIGNMENT){
            if(row.state()==WorkOrderState.ASSIGNED && Objects.equals(row.assigneeUserId(),assigneeUserId)) return;
            throw new IllegalStateException("work order is not pending assignment");
        }
        Task task=requireTask(row.processInstanceId(),"dispatchTask");
        taskService.complete(task.getId(),Map.of("assigneeUserId",assigneeUserId));
        LocalDateTime now=LocalDateTime.now();
        jdbc.update("""
            UPDATE operation_work_order
            SET status='ASSIGNED',assignee_user_id=?,dispatcher_user_id=?,
                first_response_time=COALESCE(first_response_time,?),update_time=?
            WHERE id=? AND status='PENDING_ASSIGNMENT'
            """,assigneeUserId,operator,now,now,row.id());
        events.appendEvent(tenant,row.id(),workOrderNo,"ASSIGNED",operator);
    }

    @Transactional
    public void startRepair(String workOrderNo){
        long tenant=RequestContext.requireTenantId();
        long user=RequestContext.requireUserId();
        WorkOrderRow row=loadForUpdate(tenant,workOrderNo);
        requireAssignee(row,user);
        if(row.state()==WorkOrderState.IN_PROGRESS) return;
        if(row.state()!=WorkOrderState.ASSIGNED) throw new IllegalStateException("work order cannot start repair");
        Task task=requireTask(row.processInstanceId(),"repairTask");
        if(task.getAssignee()==null) taskService.claim(task.getId(),String.valueOf(user));
        else if(!String.valueOf(user).equals(task.getAssignee())) throw new IllegalStateException("Flowable task belongs to another user");
        LocalDateTime now=LocalDateTime.now();
        jdbc.update("UPDATE operation_work_order SET status='IN_PROGRESS',repair_started_time=?,update_time=? WHERE id=? AND status='ASSIGNED'",
                now,now,row.id());
        events.appendEvent(tenant,row.id(),workOrderNo,"REPAIR_STARTED",user);
    }

    @Transactional
    public void completeRepair(String workOrderNo,String summary){
        long tenant=RequestContext.requireTenantId();
        long user=RequestContext.requireUserId();
        WorkOrderRow row=loadForUpdate(tenant,workOrderNo);
        requireAssignee(row,user);
        if(row.state()==WorkOrderState.WAIT_VERIFY) return;
        if(row.state()!=WorkOrderState.IN_PROGRESS) throw new IllegalStateException("work order is not in progress");
        Task task=requireTask(row.processInstanceId(),"repairTask");
        if(task.getAssignee()==null) taskService.claim(task.getId(),String.valueOf(user));
        taskService.complete(task.getId(),Map.of("repairSummary",summary==null?"":summary));
        LocalDateTime now=LocalDateTime.now();
        jdbc.update("""
            UPDATE operation_work_order
            SET status='WAIT_VERIFY',repair_summary=?,repair_completed_time=?,update_time=?
            WHERE id=? AND status='IN_PROGRESS'
            """,summary,now,now,row.id());
        events.appendEvent(tenant,row.id(),workOrderNo,"REPAIR_COMPLETED",user);
    }

    @Transactional
    public void verify(String workOrderNo,boolean passed,String comment){
        long tenant=RequestContext.requireTenantId();
        long verifier=RequestContext.requireUserId();
        WorkOrderRow row=loadForUpdate(tenant,workOrderNo);
        if(row.state()!=WorkOrderState.WAIT_VERIFY) throw new IllegalStateException("work order is not waiting for verification");
        if(row.assigneeUserId()!=null && row.assigneeUserId()==verifier)
            throw new IllegalStateException("repair assignee cannot verify own work");
        Task task=requireTask(row.processInstanceId(),"verifyTask");
        taskService.complete(task.getId(),Map.of("verified",passed,"verifyComment",comment==null?"":comment));
        LocalDateTime now=LocalDateTime.now();
        if(passed){
            jdbc.update("""
                UPDATE operation_work_order
                SET status='CLOSED',verifier_user_id=?,verify_comment=?,resolved_time=?,update_time=?
                WHERE id=? AND status='WAIT_VERIFY'
                """,verifier,comment,now,now,row.id());
            events.appendEvent(tenant,row.id(),workOrderNo,"VERIFIED_CLOSED",verifier);
        }else{
            jdbc.update("""
                UPDATE operation_work_order
                SET status='IN_PROGRESS',verifier_user_id=?,verify_comment=?,update_time=?
                WHERE id=? AND status='WAIT_VERIFY'
                """,verifier,comment,now,row.id());
            events.appendEvent(tenant,row.id(),workOrderNo,"VERIFY_REJECTED",verifier);
        }
    }

    private WorkOrderRow loadForUpdate(long tenant,String no){
        List<WorkOrderRow> rows=jdbc.query("""
            SELECT id,status,assignee_user_id,process_instance_id
            FROM operation_work_order WHERE tenant_id=? AND work_order_no=? FOR UPDATE
            """,(rs,n)->new WorkOrderRow(rs.getLong(1),WorkOrderState.valueOf(rs.getString(2)),
                (Long)rs.getObject(3),rs.getString(4)),tenant,no);
        if(rows.isEmpty()) throw new IllegalArgumentException("work order not found");
        return rows.get(0);
    }

    private Task requireTask(String processInstanceId,String definitionKey){
        Task task=taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .taskDefinitionKey(definitionKey)
                .singleResult();
        if(task==null) throw new IllegalStateException("expected Flowable task not found: "+definitionKey);
        return task;
    }

    private void requireAssignee(WorkOrderRow row,long user){
        if(row.assigneeUserId()==null||row.assigneeUserId()!=user)
            throw new IllegalStateException("only assigned engineer can perform this action");
    }

    private record WorkOrderRow(long id,WorkOrderState state,Long assigneeUserId,String processInstanceId){}
}
