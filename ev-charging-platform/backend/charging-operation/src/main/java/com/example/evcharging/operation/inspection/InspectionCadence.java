package com.example.evcharging.operation.inspection;

import java.time.LocalDate;

public final class InspectionCadence {
    private InspectionCadence(){}
    public static LocalDate next(LocalDate current,int cycleDays){
        if(current==null) throw new IllegalArgumentException("current date required");
        if(cycleDays<=0||cycleDays>3650) throw new IllegalArgumentException("cycleDays out of range");
        return current.plusDays(cycleDays);
    }
}
