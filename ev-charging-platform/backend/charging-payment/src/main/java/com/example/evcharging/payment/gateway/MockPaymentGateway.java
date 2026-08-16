package com.example.evcharging.payment.gateway;
import com.example.evcharging.payment.domain.PaymentChannel;
import com.example.evcharging.payment.domain.PaymentStatus;
import org.springframework.stereotype.Component;
@Component
public class MockPaymentGateway implements PaymentGateway {
  @Override public PaymentChannel channel(){return PaymentChannel.MOCK;}
  @Override public CreateResult create(String paymentNo,long amountFen,String currency){return new CreateResult(PaymentStatus.PENDING,"MOCK-"+paymentNo,"mock://pay/"+paymentNo);}
  @Override public QueryResult query(String paymentNo,String channelTradeNo){return new QueryResult(PaymentStatus.UNKNOWN,channelTradeNo);}
}
