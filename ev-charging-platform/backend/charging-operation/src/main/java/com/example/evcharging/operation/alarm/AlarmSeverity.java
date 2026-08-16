package com.example.evcharging.operation.alarm;

public enum AlarmSeverity {
    INFO(0), WARNING(1), MAJOR(2), CRITICAL(3);
    private final int rank;
    AlarmSeverity(int rank){this.rank=rank;}
    public int rank(){return rank;}
    public static AlarmSeverity parse(String value){
        if(value==null||value.isBlank()) return WARNING;
        return AlarmSeverity.valueOf(value.trim().toUpperCase());
    }
    public static AlarmSeverity max(AlarmSeverity a, AlarmSeverity b){
        return a.rank>=b.rank?a:b;
    }
}
