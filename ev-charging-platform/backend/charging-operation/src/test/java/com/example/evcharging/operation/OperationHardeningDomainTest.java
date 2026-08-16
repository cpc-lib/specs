package com.example.evcharging.operation;

import com.example.evcharging.operation.inspection.InspectionCadence;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class OperationHardeningDomainTest {
    @Test void inspectionCadenceIsDeterministic(){
        assertEquals(LocalDate.of(2026,8,17),InspectionCadence.next(LocalDate.of(2026,8,10),7));
    }
    @Test void invalidInspectionCadenceRejected(){
        assertThrows(IllegalArgumentException.class,()->InspectionCadence.next(LocalDate.now(),0));
    }
}
