package com.example.evcharging.core.billing.application;

import com.example.evcharging.core.billing.domain.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.*;
import java.util.List;
import java.util.Map;

@Service
public class BillingReplayService {
    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper;
    private final TimeOfUseBillingEngine engine = new TimeOfUseBillingEngine();

    public BillingReplayService(JdbcTemplate jdbc, ObjectMapper mapper) { this.jdbc=jdbc; this.mapper=mapper; }

    public ReplayResult replay(long tenantId, String sessionNo) throws Exception {
        Map<String,Object> s = jdbc.queryForMap("SELECT id,billing_snapshot_id,initial_meter_wh,final_meter_wh,charging_start_time,charging_end_time FROM charging_session WHERE tenant_id=? AND session_no=?", tenantId, sessionNo);
        if (s.get("initial_meter_wh")==null || s.get("final_meter_wh")==null || s.get("charging_start_time")==null || s.get("charging_end_time")==null)
            throw new IllegalStateException("session is not fully billable");
        long sessionId=((Number)s.get("id")).longValue(); long snapshotId=((Number)s.get("billing_snapshot_id")).longValue();
        BillingSnapshotDocument snapshot=mapper.readValue(jdbc.queryForObject("SELECT snapshot_json FROM charging_billing_snapshot WHERE id=?",String.class,snapshotId),BillingSnapshotDocument.class);
        Instant start=dbInstant(s.get("charging_start_time")), end=dbInstant(s.get("charging_end_time"));
        List<MeterPoint> points=jdbc.query("SELECT meter_wh,record_time FROM charging_meter_record WHERE session_id=? AND validation_status=1 ORDER BY record_time",
                (rs,n)->new MeterPoint(rs.getObject("record_time",LocalDateTime.class).toInstant(ZoneOffset.UTC),rs.getLong("meter_wh")),sessionId);
        TimeOfUseBillingResult recalculated=engine.calculate(new TimeOfUseBillingContext(ZoneId.of(snapshot.timezone()),start,end,
                ((Number)s.get("initial_meter_wh")).longValue(),((Number)s.get("final_meter_wh")).longValue(),points,snapshot.periods()));
        Map<String,Object> original=jdbc.queryForMap("SELECT energy_wh,energy_amount_fen,service_amount_fen,receivable_amount_fen FROM charging_billing_result WHERE tenant_id=? AND session_id=?",tenantId,sessionId);
        long originalAmount=((Number)original.get("receivable_amount_fen")).longValue();
        return new ReplayResult(sessionNo,snapshot.versionNo(),originalAmount,recalculated.receivableAmountFen(),recalculated.receivableAmountFen()-originalAmount,
                originalAmount==recalculated.receivableAmountFen(),recalculated);
    }

    private static Instant dbInstant(Object value){
        if(value instanceof LocalDateTime local)return local.toInstant(ZoneOffset.UTC);
        if(value instanceof Timestamp ts)return ts.toLocalDateTime().toInstant(ZoneOffset.UTC);
        throw new IllegalArgumentException("unsupported database time: "+value);
    }

    public record ReplayResult(String sessionNo,String billingVersion,long originalAmountFen,long recalculatedAmountFen,long differenceFen,boolean consistent,TimeOfUseBillingResult result){}
}
