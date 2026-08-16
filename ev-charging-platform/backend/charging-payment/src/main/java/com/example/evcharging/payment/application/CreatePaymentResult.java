package com.example.evcharging.payment.application;
public record CreatePaymentResult(String paymentNo,String orderNo,String channel,String status,long amountFen,String paymentToken) {}
