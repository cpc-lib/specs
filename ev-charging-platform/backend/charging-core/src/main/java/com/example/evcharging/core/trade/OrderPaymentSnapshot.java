package com.example.evcharging.core.trade;
public record OrderPaymentSnapshot(String orderNo,long userId,long stationId,long receivableAmountFen,long paidAmountFen,String tradeStatus,String paymentStatus) {}
