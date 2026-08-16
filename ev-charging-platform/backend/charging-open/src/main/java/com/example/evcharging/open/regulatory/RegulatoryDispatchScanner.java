package com.example.evcharging.open.regulatory;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.core.task.TaskExecutor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskRejectedException;

import java.util.List;

@Component
public class RegulatoryDispatchScanner {
    private final JdbcTemplate jdbc;private final RegulatoryDispatchWorker worker;private final TaskExecutor executor;
    public RegulatoryDispatchScanner(JdbcTemplate jdbc,RegulatoryDispatchWorker worker,@Qualifier("ioBoundedExecutor") TaskExecutor executor){this.jdbc=jdbc;this.worker=worker;this.executor=executor;}

    @Scheduled(fixedDelayString="${charging.open.regulatory-scan-ms:15000}")
    public void scan(){
        List<Long> ids=jdbc.query("""
            SELECT id FROM open_regulatory_report_task
            WHERE status IN ('PENDING','RETRY') AND next_retry_time<=UTC_TIMESTAMP(3)
            ORDER BY next_retry_time,id LIMIT 100
            """,(rs,n)->rs.getLong(1));
        for(Long id:ids){try{executor.execute(()->worker.send(id));}catch(TaskRejectedException saturated){break;}}
    }
}
