package com.example.evcharging.core.asset;
public record ConnectorSnapshot(long connectorId,long stationId,long chargerId,String connectorCode,int connectorNo,String deviceId,int onlineStatus,int runningStatus,long ratedPowerW) {}
