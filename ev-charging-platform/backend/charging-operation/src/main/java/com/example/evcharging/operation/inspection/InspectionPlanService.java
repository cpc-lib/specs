package com.example.evcharging.operation.inspection;

import com.example.evcharging.framework.context.RequestContext;
import com.example.evcharging.framework.id.IdGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.Map;

@Service
public class InspectionPlanService {
    private final JdbcTemplate jdbc;private final IdGenerator ids;private final ObjectMapper mapper;
    public InspectionPlanService(JdbcTemplate jdbc,IdGenerator ids,ObjectMapper mapper){this.jdbc=jdbc;this.ids=ids;this.mapper=mapper;}

    public Map<String,Object> create(CreatePlanCommand c){
        long tenant=RequestContext.requireTenantId();
        InspectionCadence.next(LocalDate.now(),c.cycleDays());
        if(c.stationId()<=0) throw new IllegalArgumentException("stationId required");
        if(c.planCode()==null||c.planCode().isBlank()) throw new IllegalArgumentException("planCode required");
        long id=ids.nextId();LocalDateTime now=LocalDateTime.now();
        LocalDate first=c.firstGenerateDate()==null?LocalDate.now():c.firstGenerateDate();
        jdbc.update("""
            INSERT INTO operation_inspection_plan(
              id,tenant_id,plan_code,plan_name,station_id,cycle_days,assignee_user_id,
              checklist_json,enabled,next_generate_date,create_time,update_time
            ) VALUES (?,?,?,?,?,?,?,?,1,?,?,?)
            """,id,tenant,c.planCode(),c.planName(),c.stationId(),c.cycleDays(),c.assigneeUserId(),
            json(c.checklist()),first,now,now);
        return Map.of("planId",id,"nextGenerateDate",first.toString());
    }

    private String json(JsonNode value){
        try{return mapper.writeValueAsString(value==null?mapper.createArrayNode():value);}
        catch(Exception e){throw new IllegalArgumentException("invalid checklist",e);}
    }

    public record CreatePlanCommand(String planCode,String planName,long stationId,int cycleDays,
                                    Long assigneeUserId,LocalDate firstGenerateDate,JsonNode checklist){}
}
