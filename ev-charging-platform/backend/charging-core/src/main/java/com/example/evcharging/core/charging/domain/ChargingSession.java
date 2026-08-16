package com.example.evcharging.core.charging.domain;

import java.time.Instant;

public final class ChargingSession {
    private final long id;
    private final String sessionNo;
    private final long connectorId;
    private ChargingSessionStatus status;
    private Long initialMeterWh;
    private Long finalMeterWh;
    private Instant chargingStartTime;
    private Instant chargingEndTime;

    public ChargingSession(long id, String sessionNo, long connectorId) {
        if (id <= 0) throw new IllegalArgumentException("id must be positive");
        if (sessionNo == null || sessionNo.isBlank()) throw new IllegalArgumentException("sessionNo is required");
        if (connectorId <= 0) throw new IllegalArgumentException("connectorId must be positive");
        this.id = id;
        this.sessionNo = sessionNo;
        this.connectorId = connectorId;
        this.status = ChargingSessionStatus.CREATED;
    }

    public void startRequested() {
        require(ChargingSessionStatus.CREATED);
        status = ChargingSessionStatus.STARTING;
    }

    public void prepare() {
        require(ChargingSessionStatus.STARTING);
        status = ChargingSessionStatus.PREPARING;
    }

    public void startCharging(long meterWh, Instant occurredAt) {
        require(ChargingSessionStatus.PREPARING);
        if (meterWh < 0) throw new IllegalArgumentException("meterWh cannot be negative");
        if (occurredAt == null) throw new IllegalArgumentException("occurredAt is required");
        initialMeterWh = meterWh;
        chargingStartTime = occurredAt;
        status = ChargingSessionStatus.CHARGING;
    }

    public void requestStop() {
        if (status == ChargingSessionStatus.STOPPING
                || status == ChargingSessionStatus.CHARGE_FINISHED
                || status == ChargingSessionStatus.BILLING
                || status == ChargingSessionStatus.FINISHED) return;
        require(ChargingSessionStatus.CHARGING);
        status = ChargingSessionStatus.STOPPING;
    }

    public void deviceStopped(long meterWh, Instant occurredAt) {
        if (status == ChargingSessionStatus.CHARGE_FINISHED
                || status == ChargingSessionStatus.BILLING
                || status == ChargingSessionStatus.FINISHED) return;
        if (status != ChargingSessionStatus.CHARGING && status != ChargingSessionStatus.STOPPING) {
            throw new IllegalStateException("illegal session transition: " + status + " -> CHARGE_FINISHED");
        }
        if (initialMeterWh == null) throw new IllegalStateException("initial meter is missing");
        if (meterWh < initialMeterWh) throw new IllegalArgumentException("final meter cannot be less than initial meter");
        if (occurredAt == null) throw new IllegalArgumentException("occurredAt is required");
        finalMeterWh = meterWh;
        chargingEndTime = occurredAt;
        status = ChargingSessionStatus.CHARGE_FINISHED;
    }

    public long energyWh() {
        if (initialMeterWh == null || finalMeterWh == null) throw new IllegalStateException("session does not have final meter");
        return finalMeterWh - initialMeterWh;
    }

    private void require(ChargingSessionStatus expected) {
        if (status != expected) throw new IllegalStateException("expected " + expected + " but was " + status);
    }

    public long id() { return id; }
    public String sessionNo() { return sessionNo; }
    public long connectorId() { return connectorId; }
    public ChargingSessionStatus status() { return status; }
    public Long initialMeterWh() { return initialMeterWh; }
    public Long finalMeterWh() { return finalMeterWh; }
    public Instant chargingStartTime() { return chargingStartTime; }
    public Instant chargingEndTime() { return chargingEndTime; }
}
