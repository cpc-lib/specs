package com.example.evcharging.iot.lifecycle;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HeartbeatDeadlineMemberTest {
    @Test void encodingRoundTripPreservesPipeInsideLease(){
        var original=new HeartbeatDeadlineMember(1001,"CP-001","gateway-a|token-123");
        assertEquals(original,HeartbeatDeadlineMember.parse(original.encode()));
    }
    @Test void rejectsInvalidTenant(){
        assertThrows(IllegalArgumentException.class,()->new HeartbeatDeadlineMember(0,"CP","g|t"));
    }
}
