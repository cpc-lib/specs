package com.example.evcharging.finance.invoice;

public interface InvoiceProvider {
    String code();
    IssueResult issue(IssueCommand command);
    RedResult redFlush(RedCommand command);

    record IssueCommand(String requestNo,String paymentNo,String bizOrderNo,long amountFen,
                        String titleType,String titleName,String taxNo,String email){}
    record IssueResult(String providerInvoiceNo,String pdfUrl){}
    record RedCommand(String redNo,String providerInvoiceNo,String reason){}
    record RedResult(String providerRedNo){}
}
