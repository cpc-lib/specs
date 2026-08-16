package com.example.evcharging.operation.sla;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SlaBreachScanner {
    private final JdbcTemplate jdbc;
    private final SlaBreachWorker worker;

    public SlaBreachScanner(JdbcTemplate jdbc,SlaBreachWorker worker){
        this.jdbc=jdbc;this.worker=worker;
    }

    @Scheduled(fixedDelayString="${charging.operation.sla-scan-ms:60000}")
    public void scan(){
        List<Long> ids=jdbc.query("""
            SELECT id FROM operation_work_order
            WHERE status NOT IN ('CLOSED','CANCELLED')
              AND (
                (first_response_time IS NULL AND response_due_time<NOW(3))
                OR resolution_due_time<NOW(3)
              )
            ORDER BY id LIMIT 200
            """,(rs,n)->rs.getLong(1));
        for(Long id:ids) worker.scanOne(id);
    }
}
