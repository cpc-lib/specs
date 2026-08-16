package com.example.evcharging.core.billing.domain;
public record BillingResult(long energyWh,long energyAmountFen,long serviceAmountFen,long parkingAmountFen,long occupationAmountFen,long discountAmountFen,long receivableAmountFen) {}
