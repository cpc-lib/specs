package com.example.evcharging.finance.invoice;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InvoiceProviderContractTest {
    @Test void mockProviderSupportsIssueAndRedFlush(){
        MockInvoiceProvider provider=new MockInvoiceProvider();
        var issued=provider.issue(new InvoiceProvider.IssueCommand("IR1","PO1","CO1",10000,"PERSONAL","Test",null,"a@example.com"));
        assertNotNull(issued.providerInvoiceNo());
        var red=provider.redFlush(new InvoiceProvider.RedCommand("RED1",issued.providerInvoiceNo(),"refund"));
        assertNotNull(red.providerRedNo());
    }
}
