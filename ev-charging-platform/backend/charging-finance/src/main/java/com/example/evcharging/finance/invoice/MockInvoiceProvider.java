package com.example.evcharging.finance.invoice;

import org.springframework.stereotype.Component;

@Component
public class MockInvoiceProvider implements InvoiceProvider {
    @Override public String code(){return "MOCK";}
    @Override public IssueResult issue(IssueCommand command){return new IssueResult("MOCK-INV-"+command.requestNo(),"mock://invoice/"+command.requestNo()+".pdf");}
    @Override public RedResult redFlush(RedCommand command){return new RedResult("MOCK-RED-"+command.redNo());}
}
