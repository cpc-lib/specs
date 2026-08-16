package com.example.evcharging.payment.gateway;
import com.example.evcharging.payment.domain.PaymentChannel;
import com.example.evcharging.payment.domain.PaymentStatus;
public interface PaymentGateway {
  PaymentChannel channel();
  CreateResult create(String paymentNo,long amountFen,String currency);
  QueryResult query(String paymentNo,String channelTradeNo);
  record CreateResult(PaymentStatus status,String channelTradeNo,String paymentToken){}
  record QueryResult(PaymentStatus status,String channelTradeNo){}
}
