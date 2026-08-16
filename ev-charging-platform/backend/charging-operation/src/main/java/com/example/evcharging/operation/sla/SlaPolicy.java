package com.example.evcharging.operation.sla;

import java.time.*;

public record SlaPolicy(int responseMinutes,int resolutionMinutes) {
    public SlaPolicy {
        if(responseMinutes<=0||resolutionMinutes<=0||resolutionMinutes<responseMinutes)
            throw new IllegalArgumentException("invalid SLA policy");
    }
    public DueTimes dueFrom(LocalDateTime createdAt){
        return new DueTimes(createdAt.plusMinutes(responseMinutes),createdAt.plusMinutes(resolutionMinutes));
    }
    public record DueTimes(LocalDateTime responseDueTime,LocalDateTime resolutionDueTime){}
}
