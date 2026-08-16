package com.example.evcharging.finance.invoice;

import com.example.evcharging.framework.context.RequestContext;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InvoiceApplicationService {
    private final InvoicePersistenceService persistence;private final List<InvoiceProvider> providers;
    public InvoiceApplicationService(InvoicePersistenceService persistence,List<InvoiceProvider> providers){this.persistence=persistence;this.providers=providers;}
    public record CreateRequest(String requestId,String paymentNo,String titleType,String titleName,String taxNo,String email,String providerCode){}
    public record RedRequest(String requestId,String reason,String providerCode){}

    public String issue(CreateRequest request){
        InvoicePersistenceService.ReserveResult reserved=persistence.reserve(request);
        if("SUCCESS".equals(reserved.status()) && reserved.invoiceNo()!=null) return reserved.invoiceNo();
        InvoiceProvider provider=provider(reserved.providerCode());
        try{
            var result=provider.issue(new InvoiceProvider.IssueCommand(reserved.requestNo(),reserved.paymentNo(),reserved.orderNo(),reserved.amountFen(),reserved.titleType(),reserved.titleName(),reserved.taxNo(),reserved.email()));
            return persistence.completeIssue(reserved,result);
        }catch(Exception e){persistence.failIssue(reserved.id(),e.getMessage());if(e instanceof RuntimeException runtime)throw runtime;throw new IllegalStateException("invoice provider failed",e);}
    }

    public String redFlush(String invoiceNo,RedRequest request){
        RequestContext.requireTenantId();
        InvoicePersistenceService.RedReserve reserve=persistence.reserveRed(invoiceNo,request.requestId(),request.reason());
        if("SUCCESS".equals(reserve.status()))return reserve.redNo();
        InvoiceProvider provider=provider(request.providerCode()==null||request.providerCode().isBlank()?"MOCK":request.providerCode().toUpperCase());
        try{var result=provider.redFlush(new InvoiceProvider.RedCommand(reserve.redNo(),reserve.providerInvoiceNo(),reserve.reason()));
        persistence.completeRed(reserve,result);return reserve.redNo();}
        catch(Exception e){persistence.failRed(reserve.id());if(e instanceof RuntimeException runtime)throw runtime;throw new IllegalStateException("invoice red flush provider failed",e);}
    }

    private InvoiceProvider provider(String code){return providers.stream().filter(p->p.code().equalsIgnoreCase(code)).findFirst().orElseThrow(()->new IllegalArgumentException("invoice provider not found: "+code));}
}
