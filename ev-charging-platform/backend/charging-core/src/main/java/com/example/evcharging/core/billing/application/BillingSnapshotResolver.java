package com.example.evcharging.core.billing.application;

import com.example.evcharging.core.billing.domain.PricingPeriod;
import com.example.evcharging.framework.id.IdGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BillingSnapshotResolver {
    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper;
    private final IdGenerator ids;
    private final String defaultTimezone;
    private final String fallbackVersion;

    public BillingSnapshotResolver(JdbcTemplate jdbc, ObjectMapper mapper, IdGenerator ids,
            @Value("${charging.billing.default-timezone:Asia/Shanghai}") String defaultTimezone,
            @Value("${charging.billing.fallback-version:DEV_TOU_V1}") String fallbackVersion) {
        this.jdbc = jdbc;
        this.mapper = mapper;
        this.ids = ids;
        this.defaultTimezone = defaultTimezone;
        this.fallbackVersion = fallbackVersion;
    }

    public ResolvedSnapshot resolveAndPersist(long tenantId, long stationId, long sessionId) {
        BillingSnapshotDocument doc = resolve(tenantId, stationId);
        long snapshotId = ids.nextId();
        try {
            String json = mapper.writeValueAsString(doc);
            jdbc.update("INSERT INTO charging_billing_snapshot(id,tenant_id,session_id,billing_template_id,billing_version_id,snapshot_json,snapshot_hash,create_time) VALUES (?,?,?,?,?,?,SHA2(?,256),?)",
                    snapshotId, tenantId, sessionId, doc.templateId(), doc.versionId(), json, json, LocalDateTime.now());
            return new ResolvedSnapshot(snapshotId, doc);
        } catch (Exception e) {
            throw new IllegalStateException("cannot persist billing snapshot", e);
        }
    }

    public BillingSnapshotDocument resolve(long tenantId, long stationId) {
        List<VersionRow> rows = jdbc.query("""
                SELECT v.id version_id,v.template_id,v.version_no,t.timezone
                FROM billing_station_binding b
                JOIN billing_version v ON v.id=b.billing_version_id AND v.tenant_id=b.tenant_id
                JOIN billing_template t ON t.id=v.template_id AND t.tenant_id=v.tenant_id
                WHERE b.tenant_id=? AND b.station_id=? AND v.status=1
                  AND v.effective_from<=NOW(3) AND (v.effective_to IS NULL OR v.effective_to>NOW(3))
                """, (rs, rowNum) -> new VersionRow(rs.getLong("version_id"), rs.getLong("template_id"), rs.getString("version_no"), rs.getString("timezone")), tenantId, stationId);
        if (rows.isEmpty()) return fallback();
        VersionRow row = rows.getFirst();
        return new BillingSnapshotDocument(row.versionNo(), row.templateId(), row.versionId(), row.timezone(), loadPeriods(tenantId, row.versionId()));
    }

    private List<PricingPeriod> loadPeriods(long tenantId, long versionId) {
        return jdbc.query("SELECT sequence_no,period_type,start_minute,end_minute,energy_price_micro,service_price_micro FROM billing_period WHERE tenant_id=? AND version_id=? ORDER BY sequence_no",
                (rs, n) -> new PricingPeriod(rs.getInt(1), rs.getString(2), rs.getInt(3), rs.getInt(4), rs.getLong(5), rs.getLong(6)), tenantId, versionId);
    }

    private BillingSnapshotDocument fallback() {
        return new BillingSnapshotDocument(fallbackVersion, 0, 0, defaultTimezone, List.of(
                new PricingPeriod(1, "VALLEY", 0, 480, 350000, 300000),
                new PricingPeriod(2, "PEAK", 480, 720, 1050000, 300000),
                new PricingPeriod(3, "FLAT", 720, 1080, 650000, 300000),
                new PricingPeriod(4, "PEAK", 1080, 1320, 1050000, 300000),
                new PricingPeriod(5, "VALLEY", 1320, 1440, 350000, 300000)
        ));
    }

    private record VersionRow(long versionId,long templateId,String versionNo,String timezone) {}

    public record ResolvedSnapshot(long snapshotId, BillingSnapshotDocument document) {}
}
