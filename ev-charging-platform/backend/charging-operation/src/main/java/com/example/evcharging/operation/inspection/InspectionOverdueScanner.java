package com.example.evcharging.operation.inspection;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class InspectionOverdueScanner {
    private final JdbcTemplate jdbc;
    public InspectionOverdueScanner(JdbcTemplate jdbc){this.jdbc=jdbc;}

    @Scheduled(cron="${charging.operation.inspection-overdue-cron:0 30 0 * * *}")
    public void scan(){
        jdbc.update("""
            UPDATE operation_inspection_task
            SET overdue=1,update_time=?
            WHERE status IN ('PENDING','IN_PROGRESS') AND scheduled_date<? AND overdue=0
            """,LocalDateTime.now(),LocalDate.now());
    }
}
