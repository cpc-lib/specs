package com.example.evcharging.payment.domain;
public final class PaymentStateMachine {
  private PaymentStateMachine(){}
  public static boolean canSucceed(PaymentStatus s){return s==PaymentStatus.CREATED||s==PaymentStatus.PENDING||s==PaymentStatus.PROCESSING||s==PaymentStatus.UNKNOWN;}
  public static boolean terminal(PaymentStatus s){return s==PaymentStatus.SUCCESS||s==PaymentStatus.FAILED||s==PaymentStatus.CLOSED;}
}
