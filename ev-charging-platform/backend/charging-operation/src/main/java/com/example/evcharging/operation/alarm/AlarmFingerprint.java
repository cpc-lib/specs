package com.example.evcharging.operation.alarm;

public final class AlarmFingerprint {
    private AlarmFingerprint(){}
    public static String of(String deviceId,Integer connectorNo,String alarmCode){
        if(deviceId==null||deviceId.isBlank()) throw new IllegalArgumentException("deviceId required");
        if(alarmCode==null||alarmCode.isBlank()) throw new IllegalArgumentException("alarmCode required");
        return deviceId.trim()+"|"+(connectorNo==null?"DEVICE":connectorNo)+"|"+alarmCode.trim().toUpperCase();
    }
}
