package com.example.evcharging.payment.integration;
public record OrderPaymentSnapshot(String orderNo,long userId,long stationId,long receivableAmountFen,long paidAmountFen,String tradeStatus,String paymentStatus) {
  public long outstandingAmountFen(){return Math.max(0,receivableAmountFen-paidAmountFen);}
}
