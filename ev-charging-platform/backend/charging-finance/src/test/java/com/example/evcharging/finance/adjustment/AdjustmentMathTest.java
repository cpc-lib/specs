package com.example.evcharging.finance.adjustment;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AdjustmentMathTest {
    @Test void appendAdjustmentChangesEffectiveFactWithoutChangingOriginal(){
        var effective=AdjustmentMath.calculate(10000,-1,1000,1);
        assertEquals(9999,effective.paymentFen());
        assertEquals(1001,effective.refundFen());
        assertEquals(8998,effective.netFen());
    }
    @Test void refundCannotExceedPayment(){assertThrows(IllegalArgumentException.class,()->AdjustmentMath.calculate(10000,0,9000,2000));}
}
