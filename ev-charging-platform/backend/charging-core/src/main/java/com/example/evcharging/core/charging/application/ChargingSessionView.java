package com.example.evcharging.core.charging.application;
public record ChargingSessionView(String sessionNo,String status,long connectorId,long energyWh,Integer soc,Long powerW,Long receivableAmountFen,String orderNo) {}
