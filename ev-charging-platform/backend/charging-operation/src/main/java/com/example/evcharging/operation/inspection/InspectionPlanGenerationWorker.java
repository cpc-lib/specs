package com.example.evcharging.operation.inspection;

import com.example.evcharging.framework.id.IdGenerator;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.List;

@Service
public class InspectionPlanGenerationWorker {
    private final JdbcTemplate jdbc;private final IdGenerator ids;
    public InspectionPlanGenerationWorker(JdbcTemplate jdbc,IdGenerator ids){this.jdbc=jdbc;this.ids=ids;}

    @Transactional(propagation=Propagation.REQUIRES_NEW)
    public void generate(long planId,LocalDate today){
        List<Plan> rows=jdbc.query("""
            SELECT tenant_id,station_id,cycle_days,assignee_user_id,checklist_json,next_generate_date,enabled
            FROM operation_inspection_plan WHERE id=? FOR UPDATE
            """,(rs,n)->new Plan(rs.getLong(1),rs.getLong(2),rs.getInt(3),(Long)rs.getObject(4),
                rs.getString(5),rs.getObject(6,LocalDate.class),rs.getBoolean(7)),planId);
        if(rows.isEmpty()||!rows.get(0).enabled()) return;
        Plan p=rows.get(0);LocalDate due=p.nextGenerateDate();int generated=0;
        while(!due.isAfter(today)&&generated<31){
            createTask(planId,p,due);
            due=InspectionCadence.next(due,p.cycleDays());
            generated++;
        }
        jdbc.update("UPDATE operation_inspection_plan SET next_generate_date=?,update_time=? WHERE id=?",
                due,LocalDateTime.now(),planId);
    }

    private void createTask(long planId,Plan p,LocalDate due){
        long id=ids.nextId();LocalDateTime now=LocalDateTime.now();
        try{
            jdbc.update("""
                INSERT INTO operation_inspection_task(
                  id,tenant_id,task_no,plan_id,station_id,scheduled_date,status,assignee_user_id,
                  checklist_json,result_json,overdue,create_time,update_time
                ) VALUES (?,?,?,?,?,?,'PENDING',?,?,NULL,0,?,?)
                """,id,p.tenantId(),"IT"+id,planId,p.stationId(),due,p.assigneeUserId(),p.checklistJson(),now,now);
        }catch(DuplicateKeyException duplicate){
            // unique(plan_id, scheduled_date) makes scheduler retry safe
        }
    }

    private record Plan(long tenantId,long stationId,int cycleDays,Long assigneeUserId,
                        String checklistJson,LocalDate nextGenerateDate,boolean enabled){}
}
