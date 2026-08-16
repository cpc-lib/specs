package com.example.evcharging.asset.event;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class OutboxRepository {
    public static final int NEW = 0;
    public static final int PUBLISHING = 1;
    public static final int PUBLISHED = 2;
    public static final int RETRY = 3;
    public static final int DEAD = 4;

    private final JdbcTemplate jdbc;

    public OutboxRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Map<String, Object>> claimBatch(String workerId, int limit) {
        List<Long> candidateIds = jdbc.queryForList(
                "SELECT id FROM event_outbox "
                        + "WHERE ((status IN (?,?) AND (next_retry_time IS NULL OR next_retry_time <= NOW(3))) "
                        + "OR (status=? AND locked_until < NOW(3))) ORDER BY id LIMIT ?",
                Long.class, NEW, RETRY, PUBLISHING, limit);

        List<Map<String, Object>> claimed = new ArrayList<>();
        LocalDateTime lockedUntil = LocalDateTime.now().plusSeconds(30);

        for (Long id : candidateIds) {
            int updated = jdbc.update(
                    "UPDATE event_outbox SET status=?,locked_by=?,locked_until=? WHERE id=? "
                            + "AND (status IN (?,?) OR (status=? AND locked_until < NOW(3)))",
                    PUBLISHING, workerId, lockedUntil, id, NEW, RETRY, PUBLISHING);
            if (updated == 1) {
                claimed.add(jdbc.queryForMap(
                        "SELECT id,tenant_id,event_id,aggregate_type,aggregate_id,event_type,event_version,payload,trace_id,occurred_time "
                                + "FROM event_outbox WHERE id=?", id));
            }
        }
        return claimed;
    }

    public void markPublished(long id) {
        jdbc.update("UPDATE event_outbox SET status=?,published_time=NOW(3),locked_by=NULL,locked_until=NULL WHERE id=? AND status=?",
                PUBLISHED, id, PUBLISHING);
    }

    public void markFailed(long id, int currentRetryCount, String error) {
        int retryCount = currentRetryCount + 1;
        if (retryCount >= 10) {
            jdbc.update("UPDATE event_outbox SET status=?,retry_count=?,last_error=?,locked_by=NULL,locked_until=NULL WHERE id=?",
                    DEAD, retryCount, abbreviate(error), id);
            return;
        }
        long delaySeconds = Math.min(300, 1L << Math.min(retryCount, 8));
        jdbc.update("UPDATE event_outbox SET status=?,retry_count=?,last_error=?,next_retry_time=?,locked_by=NULL,locked_until=NULL WHERE id=?",
                RETRY, retryCount, abbreviate(error), LocalDateTime.now().plusSeconds(delaySeconds), id);
    }

    public int retryCount(long id) {
        Integer value = jdbc.queryForObject("SELECT retry_count FROM event_outbox WHERE id=?", Integer.class, id);
        return value == null ? 0 : value;
    }

    private static String abbreviate(String value) {
        if (value == null) return null;
        return value.length() <= 1000 ? value : value.substring(0, 1000);
    }
}
