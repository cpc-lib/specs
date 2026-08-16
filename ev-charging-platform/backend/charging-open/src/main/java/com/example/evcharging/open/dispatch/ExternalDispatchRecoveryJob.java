package com.example.evcharging.open.dispatch;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ExternalDispatchRecoveryJob {
    private final JdbcTemplate jdbc;
    public ExternalDispatchRecoveryJob(JdbcTemplate jdbc){this.jdbc=jdbc;}

    @Scheduled(fixedDelayString="${charging.open.dispatch-recovery-ms:60000}")
    public void recover(){
        jdbc.update("""
            UPDATE open_partner_callback_task
            SET status='RETRY',claim_token=NULL,claim_time=NULL,last_error='STALE_CLAIM_RECOVERED',
                next_retry_time=UTC_TIMESTAMP(3),update_time=UTC_TIMESTAMP(3)
            WHERE status='SENDING' AND claim_time<UTC_TIMESTAMP(3)-INTERVAL 5 MINUTE
            """);
        jdbc.update("""
            UPDATE open_regulatory_report_task
            SET status='RETRY',claim_token=NULL,claim_time=NULL,last_error='STALE_CLAIM_RECOVERED',
                next_retry_time=UTC_TIMESTAMP(3),update_time=UTC_TIMESTAMP(3)
            WHERE status='SENDING' AND claim_time<UTC_TIMESTAMP(3)-INTERVAL 5 MINUTE
            """);
    }
}
