package com.example.evcharging.core.charging.domain;
public enum ChargingSessionStatus {
    CREATED(0), STARTING(10), PREPARING(20), CHARGING(30), STOPPING(40), CHARGE_FINISHED(50), BILLING(60), FINISHED(70), START_FAILED(80), RECOVERING(90), ABNORMAL(100), MANUAL_REVIEW(110);
    private final int code; ChargingSessionStatus(int code){this.code=code;} public int code(){return code;}
    public static ChargingSessionStatus fromCode(int code){for(var s:values()) if(s.code==code) return s; throw new IllegalArgumentException("unknown status code: "+code);}
}
