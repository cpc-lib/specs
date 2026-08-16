package com.example.evcharging.asset.charger;
public record ConnectorSnapshot(long connectorId,long stationId,long chargerId,String connectorCode,int connectorNo,
                                String deviceId,int onlineStatus,int runningStatus,long ratedPowerW) {}
