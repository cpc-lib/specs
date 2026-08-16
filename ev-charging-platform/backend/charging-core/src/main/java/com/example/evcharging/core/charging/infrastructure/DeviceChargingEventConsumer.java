package com.example.evcharging.core.charging.infrastructure;

import com.example.evcharging.core.billing.application.BillingSnapshotDocument;
import com.example.evcharging.core.billing.domain.*;
import com.example.evcharging.core.charging.domain.ChargingSessionStatus;
import com.example.evcharging.core.charging.realtime.ChargingRealtimeHub;
import com.example.evcharging.framework.contract.DeviceChargingEvent;
import com.example.evcharging.framework.event.DomainEventEnvelope;
import com.example.evcharging.framework.id.IdGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.*;
import java.util.*;

@Component
public class DeviceChargingEventConsumer {
    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper;
    private final StringRedisTemplate redis;
    private final IdGenerator ids;
    private final ChargingRealtimeHub realtime;
    private final TimeOfUseBillingEngine billing = new TimeOfUseBillingEngine();

    public DeviceChargingEventConsumer(JdbcTemplate jdbc, ObjectMapper mapper, StringRedisTemplate redis, IdGenerator ids, ChargingRealtimeHub realtime) {
        this.jdbc = jdbc; this.mapper = mapper; this.redis = redis; this.ids = ids; this.realtime = realtime;
    }

    @KafkaListener(topics="${charging.device-event-topic:ev.device.charging.v1}", groupId="charging-core-device-charging-v1")
    @Transactional
    public void consume(String message) throws Exception {
        DomainEventEnvelope<DeviceChargingEvent> envelope = mapper.readValue(message, new TypeReference<DomainEventEnvelope<DeviceChargingEvent>>(){});
        DeviceChargingEvent event = Objects.requireNonNull(envelope.payload(), "device event payload");
        if (event.sessionNo() == null || event.sessionNo().isBlank() || event.tenantId() <= 0) throw new IllegalArgumentException("invalid device charging event");
        try {
            jdbc.update("INSERT INTO charging_device_event_inbox(id,event_id,event_type,processed_time) VALUES (?,?,?,?)",
                    ids.nextId(), envelope.eventId(), envelope.eventType(), LocalDateTime.now());
        } catch (DuplicateKeyException duplicate) { return; }
        switch (event.eventType()) {
            case "CHARGING_STARTED" -> started(event);
            case "TELEMETRY" -> telemetry(event);
            case "CHARGING_STOPPED" -> stopped(event);
            default -> { }
        }
    }

    private void started(DeviceChargingEvent e) {
        LocalDateTime now = LocalDateTime.now();
        Instant occurred = occurredAt(e);
        LocalDateTime eventTime = LocalDateTime.ofInstant(occurred, ZoneOffset.UTC);
        jdbc.update("""
                UPDATE charging_session SET status=?,
                    initial_meter_wh=COALESCE(initial_meter_wh,?), initial_soc=COALESCE(initial_soc,?),
                    charging_start_time=COALESCE(charging_start_time,?), update_time=?
                WHERE tenant_id=? AND session_no=? AND status IN (?,?,?)
                """, ChargingSessionStatus.CHARGING.code(), e.meterWh(), e.soc(), eventTime, now,
                e.tenantId(), e.sessionNo(), ChargingSessionStatus.STARTING.code(), ChargingSessionStatus.PREPARING.code(), ChargingSessionStatus.RECOVERING.code());
        realtime.publish(e.sessionNo(), Map.of("event","CHARGING_STATUS_CHANGED","sessionNo",e.sessionNo(),"status","CHARGING","soc",e.soc(),"meterWh",e.meterWh(),"occurredAt",occurred.toString()));
    }

    private void telemetry(DeviceChargingEvent e) throws Exception {
        Map<String,Object> row = jdbc.queryForMap("SELECT id,status,initial_meter_wh FROM charging_session WHERE tenant_id=? AND session_no=?", e.tenantId(), e.sessionNo());
        int status = ((Number) row.get("status")).intValue();
        Object initial = row.get("initial_meter_wh");
        if (initial == null || (status != ChargingSessionStatus.CHARGING.code() && status != ChargingSessionStatus.STOPPING.code())) return;
        long start = ((Number) initial).longValue();
        LocalDateTime now = LocalDateTime.now();
        Instant occurred = occurredAt(e);
        LocalDateTime eventTime = LocalDateTime.ofInstant(occurred, ZoneOffset.UTC);
        long sessionId = ((Number)row.get("id")).longValue();
        List<MeterState> latest = jdbc.query("SELECT meter_wh,record_time FROM charging_meter_record WHERE session_id=? AND validation_status=1 ORDER BY record_time DESC,id DESC LIMIT 1",
                (rs,n)->new MeterState(rs.getLong("meter_wh"),rs.getObject("record_time",LocalDateTime.class)),sessionId);
        boolean valid = e.meterWh() >= start;
        if (!latest.isEmpty()) {
            MeterState previous=latest.getFirst();
            valid = valid && e.meterWh() >= previous.meterWh() && eventTime.isAfter(previous.recordTime());
        }
        jdbc.update("INSERT INTO charging_meter_record(id,tenant_id,session_id,session_no,record_type,meter_wh,soc,power_w,record_time,source,validation_status,create_time) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)",
                ids.nextId(), e.tenantId(), sessionId, e.sessionNo(), 1, e.meterWh(), e.soc(), e.powerW(), eventTime, 1, valid?1:2, now);
        if (!valid) return;
        long energy = e.meterWh() - start;
        jdbc.update("UPDATE charging_session SET energy_wh=?,update_time=? WHERE tenant_id=? AND session_no=?", energy, now, e.tenantId(), e.sessionNo());
        Map<String,Object> live = new LinkedHashMap<>();
        live.put("event","CHARGING_TELEMETRY"); live.put("sessionNo",e.sessionNo()); live.put("soc",e.soc()); live.put("powerW",e.powerW()); live.put("meterWh",e.meterWh()); live.put("energyWh",energy); live.put("occurredAt",occurred.toString());
        String json = mapper.writeValueAsString(live);
        redis.opsForValue().set("ev:"+e.tenantId()+":charging:latest:"+e.sessionNo(), json);
        realtime.publish(e.sessionNo(), live);
    }

    private void stopped(DeviceChargingEvent e) throws Exception {
        Integer existing = jdbc.queryForObject("SELECT COUNT(*) FROM charge_order WHERE tenant_id=? AND session_no=?", Integer.class, e.tenantId(), e.sessionNo());
        if (existing != null && existing > 0) return;
        Map<String,Object> s = jdbc.queryForMap("SELECT id,connector_id,initial_meter_wh,billing_snapshot_id,charging_start_time FROM charging_session WHERE tenant_id=? AND session_no=?", e.tenantId(), e.sessionNo());
        if (s.get("initial_meter_wh") == null || s.get("charging_start_time") == null) {
            jdbc.update("UPDATE charging_session SET status=?,update_time=? WHERE id=?", ChargingSessionStatus.MANUAL_REVIEW.code(), LocalDateTime.now(), s.get("id"));
            return;
        }
        long sessionId = ((Number)s.get("id")).longValue();
        long startMeter = ((Number)s.get("initial_meter_wh")).longValue();
        if (e.meterWh() < startMeter) {
            jdbc.update("UPDATE charging_session SET status=?,update_time=? WHERE id=?", ChargingSessionStatus.MANUAL_REVIEW.code(), LocalDateTime.now(), sessionId);
            return;
        }
        long snapshotId = ((Number)s.get("billing_snapshot_id")).longValue();
        String snapshotJson = jdbc.queryForObject("SELECT snapshot_json FROM charging_billing_snapshot WHERE id=?", String.class, snapshotId);
        BillingSnapshotDocument snapshot = mapper.readValue(snapshotJson, BillingSnapshotDocument.class);
        Instant chargingStart = dbInstant(s.get("charging_start_time"));
        Instant chargingEnd = occurredAt(e);
        List<MeterState> latestValid = jdbc.query("SELECT meter_wh,record_time FROM charging_meter_record WHERE session_id=? AND validation_status=1 ORDER BY record_time DESC,id DESC LIMIT 1",
                (rs,n)->new MeterState(rs.getLong("meter_wh"),rs.getObject("record_time",LocalDateTime.class)),sessionId);
        if (!chargingEnd.isAfter(chargingStart) || (!latestValid.isEmpty() && e.meterWh() < latestValid.getFirst().meterWh())) {
            jdbc.update("UPDATE charging_session SET status=?,update_time=? WHERE id=?", ChargingSessionStatus.MANUAL_REVIEW.code(), LocalDateTime.now(), sessionId);
            return;
        }
        List<MeterPoint> points = jdbc.query("SELECT meter_wh,record_time FROM charging_meter_record WHERE session_id=? AND validation_status=1 ORDER BY record_time",
                (rs,n) -> new MeterPoint(rs.getObject("record_time", LocalDateTime.class).toInstant(ZoneOffset.UTC), rs.getLong("meter_wh")), sessionId);
        TimeOfUseBillingResult result = billing.calculate(new TimeOfUseBillingContext(
                ZoneId.of(snapshot.timezone()), chargingStart, chargingEnd, startMeter, e.meterWh(), points, snapshot.periods()));
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime finishEventTime = LocalDateTime.ofInstant(chargingEnd, ZoneOffset.UTC);
        for (BillingSegment segment : result.segments()) {
            jdbc.update("INSERT INTO charging_segment(id,tenant_id,session_id,segment_no,period_type,start_time,end_time,start_meter_wh,end_meter_wh,energy_wh,energy_price_micro,service_price_micro,energy_amount_fen,service_amount_fen,create_time) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                    ids.nextId(), e.tenantId(), sessionId, segment.sequence(), segment.periodType(), LocalDateTime.ofInstant(segment.start(),ZoneOffset.UTC), LocalDateTime.ofInstant(segment.end(),ZoneOffset.UTC),
                    segment.startMeterWh(), segment.endMeterWh(), segment.energyWh(), segment.energyPriceMicro(), segment.servicePriceMicro(), segment.energyAmountFen(), segment.serviceAmountFen(), now);
        }
        String resultJson = mapper.writeValueAsString(result);
        jdbc.update("INSERT INTO charging_billing_result(id,tenant_id,session_id,snapshot_id,energy_wh,energy_amount_fen,service_amount_fen,parking_amount_fen,occupation_amount_fen,discount_amount_fen,receivable_amount_fen,result_hash,create_time) VALUES (?,?,?,?,?,?,?,?,?,?,?,SHA2(?,256),?)",
                ids.nextId(), e.tenantId(), sessionId, snapshotId, result.energyWh(), result.energyAmountFen(), result.serviceAmountFen(), result.parkingAmountFen(), result.occupationAmountFen(), result.discountAmountFen(), result.receivableAmountFen(), resultJson, now);
        long orderId = ids.nextId(); String orderNo = "CO" + orderId;
        jdbc.update("UPDATE charging_session SET status=?,final_meter_wh=?,final_soc=?,energy_wh=?,charging_end_time=?,update_time=? WHERE id=?",
                ChargingSessionStatus.FINISHED.code(), e.meterWh(), e.soc(), result.energyWh(), finishEventTime, now, sessionId);
        jdbc.update("""
                INSERT INTO charge_order(id,tenant_id,order_no,session_id,session_no,user_id,station_id,charger_id,connector_id,
                  energy_wh,energy_amount_fen,service_amount_fen,parking_amount_fen,occupation_amount_fen,original_amount_fen,
                  discount_amount_fen,receivable_amount_fen,paid_amount_fen,refunded_amount_fen,trade_status,payment_status,
                  refund_status,invoice_status,finish_time,version,create_time,update_time)
                SELECT ?,tenant_id,?,id,session_no,user_id,station_id,charger_id,connector_id,
                  ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?
                FROM charging_session WHERE id=?
                """, orderId, orderNo, result.energyWh(), result.energyAmountFen(), result.serviceAmountFen(), result.parkingAmountFen(), result.occupationAmountFen(),
                result.receivableAmountFen()+result.discountAmountFen(), result.discountAmountFen(), result.receivableAmountFen(), 0, 0, 1, 0, 0, 0, finishEventTime, 0, now, now, sessionId);
        jdbc.update("DELETE FROM connector_active_session WHERE connector_id=? AND session_id=?", s.get("connector_id"), sessionId);
        redis.delete("ev:"+e.tenantId()+":charging:latest:"+e.sessionNo());
        realtime.publish(e.sessionNo(), Map.of("event","CHARGING_STATUS_CHANGED","sessionNo",e.sessionNo(),"status","FINISHED","soc",e.soc(),"energyWh",result.energyWh(),"amountFen",result.receivableAmountFen(),"orderNo",orderNo));
    }
    private record MeterState(long meterWh, LocalDateTime recordTime) {}
    private static Instant occurredAt(DeviceChargingEvent e) { return e.occurredAt() == null ? Instant.now() : e.occurredAt(); }
    private static Instant dbInstant(Object value) {
        if (value instanceof LocalDateTime local) return local.toInstant(ZoneOffset.UTC);
        if (value instanceof Timestamp timestamp) return timestamp.toLocalDateTime().toInstant(ZoneOffset.UTC);
        throw new IllegalArgumentException("unsupported database time: " + value);
    }
}
