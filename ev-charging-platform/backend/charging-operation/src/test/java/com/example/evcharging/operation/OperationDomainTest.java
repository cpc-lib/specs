package com.example.evcharging.operation;

import com.example.evcharging.operation.alarm.*;
import com.example.evcharging.operation.sla.SlaPolicy;
import com.example.evcharging.operation.workorder.WorkOrderState;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class OperationDomainTest {
    @Test void fingerprintIsStable(){
        assertEquals("CP001|1|CONNECTOR_OVER_TEMPERATURE",
                AlarmFingerprint.of("CP001",1,"connector_over_temperature"));
    }
    @Test void severityEscalates(){
        assertEquals(AlarmSeverity.CRITICAL,AlarmSeverity.max(AlarmSeverity.WARNING,AlarmSeverity.CRITICAL));
    }
    @Test void slaDueTimesAreDeterministic(){
        LocalDateTime t=LocalDateTime.of(2026,8,10,10,0);
        var due=new SlaPolicy(10,120).dueFrom(t);
        assertEquals(t.plusMinutes(10),due.responseDueTime());
        assertEquals(t.plusMinutes(120),due.resolutionDueTime());
    }
    @Test void terminalStates(){
        assertTrue(WorkOrderState.CLOSED.terminal());
        assertFalse(WorkOrderState.IN_PROGRESS.terminal());
    }
}
