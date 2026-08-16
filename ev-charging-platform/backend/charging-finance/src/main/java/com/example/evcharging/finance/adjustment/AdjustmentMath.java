package com.example.evcharging.finance.adjustment;

public final class AdjustmentMath {
    private AdjustmentMath() {}
    public record Effective(long paymentFen,long refundFen,long netFen) {}
    public static Effective calculate(long originalPayment,long paymentAdjustment,long originalRefund,long refundAdjustment){
        long payment=Math.addExact(originalPayment,paymentAdjustment);
        long refund=Math.addExact(originalRefund,refundAdjustment);
        if(payment<0||refund<0||refund>payment)throw new IllegalArgumentException("invalid adjusted financial fact");
        return new Effective(payment,refund,Math.subtractExact(payment,refund));
    }
}
