package com.example.evcharging.core.billing.application;

import com.example.evcharging.core.billing.domain.PricingPeriod;
import com.example.evcharging.core.asset.AssetStationClient;
import com.example.evcharging.framework.id.IdGenerator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;

@Service
public class BillingAdminService {
    private final JdbcTemplate jdbc;
    private final IdGenerator ids;
    private final BillingSnapshotResolver resolver;
    private final AssetStationClient stations;

    public BillingAdminService(JdbcTemplate jdbc, IdGenerator ids, BillingSnapshotResolver resolver, AssetStationClient stations) {
        this.jdbc = jdbc;
        this.ids = ids;
        this.resolver = resolver;
        this.stations = stations;
    }

    @Transactional
    public BillingSnapshotDocument publish(long tenantId, long stationId, PublishBillingVersionRequest request) {
        requireStation(tenantId,stationId);
        ZoneId.of(request.timezone());
        List<PublishBillingVersionRequest.PeriodRequest> sortedRequests = request.periods().stream()
                .sorted(Comparator.comparingInt(PublishBillingVersionRequest.PeriodRequest::startMinute)).toList();
        java.util.ArrayList<PricingPeriod> mutablePeriods = new java.util.ArrayList<>();
        for (int i=0;i<sortedRequests.size();i++) {
            var p=sortedRequests.get(i);
            mutablePeriods.add(new PricingPeriod(i+1,p.periodType(),p.startMinute(),p.endMinute(),p.energyPriceMicro(),p.servicePriceMicro()));
        }
        List<PricingPeriod> periods = List.copyOf(mutablePeriods);
        validateCoverage(periods);
        LocalDateTime now = LocalDateTime.now();
        List<Long> templates = jdbc.query("SELECT id FROM billing_template WHERE tenant_id=? AND template_name=? AND status=1 ORDER BY create_time LIMIT 1",
                (rs,n)->rs.getLong(1), tenantId, request.templateName().trim());
        long templateId;
        if (templates.isEmpty()) {
            templateId = ids.nextId();
            jdbc.update("INSERT INTO billing_template(id,tenant_id,template_name,timezone,status,create_time,update_time) VALUES (?,?,?,?,1,?,?)",
                    templateId, tenantId, request.templateName().trim(), request.timezone(), now, now);
        } else {
            templateId = templates.getFirst();
        }
        long versionId = ids.nextId();
        jdbc.update("INSERT INTO billing_version(id,tenant_id,template_id,version_no,status,effective_from,effective_to,create_time) VALUES (?,?,?,?,1,?,NULL,?)",
                versionId, tenantId, templateId, request.versionNo().trim(), request.effectiveFrom(), now);
        for (PricingPeriod p : periods) {
            jdbc.update("INSERT INTO billing_period(id,tenant_id,version_id,sequence_no,period_type,start_minute,end_minute,energy_price_micro,service_price_micro,create_time) VALUES (?,?,?,?,?,?,?,?,?,?)",
                    ids.nextId(), tenantId, versionId, p.sequence(), p.periodType(), p.startMinute(), p.endMinute(), p.energyPriceMicro(), p.servicePriceMicro(), now);
        }
        int updated = jdbc.update("UPDATE billing_station_binding SET billing_version_id=?,update_time=? WHERE station_id=? AND tenant_id=?",
                versionId, now, stationId, tenantId);
        if (updated == 0) {
            jdbc.update("INSERT INTO billing_station_binding(station_id,tenant_id,billing_version_id,update_time) VALUES (?,?,?,?)",
                    stationId, tenantId, versionId, now);
        }
        return new BillingSnapshotDocument(request.versionNo().trim(),templateId,versionId,request.timezone(),periods);
    }

    public BillingSnapshotDocument current(long tenantId, long stationId) {
        requireStation(tenantId,stationId);
        return resolver.resolve(tenantId, stationId);
    }

    private void requireStation(long tenantId,long stationId){if(!stations.exists(stationId,tenantId))throw new IllegalArgumentException("station not found");}

    private static void validateCoverage(List<PricingPeriod> periods) {
        int cursor = 0;
        for (PricingPeriod p : periods) {
            if (p.startMinute() != cursor || p.endMinute() <= p.startMinute()) throw new IllegalArgumentException("pricing periods must cover 00:00-24:00 without gaps or overlaps");
            cursor = p.endMinute();
        }
        if (cursor != 1440) throw new IllegalArgumentException("pricing periods must end at 24:00");
    }
}
