package com.example.evcharging.framework.contract;
import java.time.Instant;
public record DeviceChargingEvent(long tenantId,String eventType,String deviceId,String sessionNo,int connectorNo,Long meterWh,Integer soc,Long powerW,String commandId,String reason,Instant occurredAt) {}
