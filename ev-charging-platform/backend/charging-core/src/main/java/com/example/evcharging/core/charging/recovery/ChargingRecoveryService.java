package com.example.evcharging.core.charging.recovery;

import com.example.evcharging.core.charging.domain.ChargingSessionStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChargingRecoveryService {
    private final JdbcTemplate jdbc;
    private final ChargingRecoveryWorker worker;
    private final boolean enabled;
    private final int startTimeout;
    private final int stopTimeout;

    public ChargingRecoveryService(JdbcTemplate jdbc, ChargingRecoveryWorker worker,
            @Value("${charging.recovery.enabled:true}") boolean enabled,
            @Value("${charging.recovery.starting-timeout-seconds:45}") int startTimeout,
            @Value("${charging.recovery.stopping-timeout-seconds:45}") int stopTimeout) {
        this.jdbc=jdbc; this.worker=worker; this.enabled=enabled; this.startTimeout=startTimeout; this.stopTimeout=stopTimeout;
    }

    @Scheduled(fixedDelayString="${charging.recovery.scan-ms:15000}")
    public void scan() {
        if (!enabled) return;
        LocalDateTime startCutoff = LocalDateTime.now().minusSeconds(startTimeout);
        LocalDateTime stopCutoff = LocalDateTime.now().minusSeconds(stopTimeout);
        List<Long> sessionIds = jdbc.query("""
                SELECT id FROM charging_session
                WHERE (status=? AND update_time<?) OR (status=? AND update_time<?) OR (status=? AND update_time<?)
                ORDER BY update_time LIMIT 100
                """, (rs,n)->rs.getLong(1), ChargingSessionStatus.STARTING.code(), startCutoff,
                ChargingSessionStatus.STOPPING.code(), stopCutoff, ChargingSessionStatus.RECOVERING.code(), stopCutoff);
        for (Long sessionId : sessionIds) {
            try { worker.recoverOne(sessionId); } catch (Exception ignored) { }
        }
    }
}
