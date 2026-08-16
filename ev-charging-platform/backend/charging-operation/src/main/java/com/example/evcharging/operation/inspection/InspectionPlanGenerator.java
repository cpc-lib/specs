package com.example.evcharging.operation.inspection;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class InspectionPlanGenerator {
    private final JdbcTemplate jdbc;private final InspectionPlanGenerationWorker worker;
    public InspectionPlanGenerator(JdbcTemplate jdbc,InspectionPlanGenerationWorker worker){this.jdbc=jdbc;this.worker=worker;}

    @Scheduled(cron="${charging.operation.inspection-generate-cron:0 5 0 * * *}")
    public void generate(){
        List<Long> ids=jdbc.query("""
            SELECT id FROM operation_inspection_plan
            WHERE enabled=1 AND next_generate_date<=?
            ORDER BY next_generate_date,id LIMIT 200
            """,(rs,n)->rs.getLong(1),LocalDate.now());
        for(Long id:ids) worker.generate(id,LocalDate.now());
    }
}
