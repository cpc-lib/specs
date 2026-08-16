package com.example.evcharging.operation.notification;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NotificationDispatchScanner {
    private final JdbcTemplate jdbc;
    private final NotificationDispatchWorker worker;

    public NotificationDispatchScanner(JdbcTemplate jdbc,NotificationDispatchWorker worker){
        this.jdbc=jdbc;this.worker=worker;
    }

    @Scheduled(fixedDelayString="${charging.operation.notification-scan-ms:15000}")
    public void scan(){
        List<Long> ids=jdbc.query("""
            SELECT id FROM operation_notification_task
            WHERE status IN ('PENDING','RETRY') AND scheduled_time<=NOW(3)
            ORDER BY scheduled_time,id LIMIT 100
            """,(rs,n)->rs.getLong(1));
        for(Long id:ids) worker.dispatch(id);
    }
}
