package com.example.evcharging.open.regulatory;

import java.util.Map;

public interface RegulatoryProtocolAdapter {
    boolean supports(String protocolCode);
    PreparedReport prepare(Platform platform,String dataType,String businessKey,String sourcePayloadJson);

    record Platform(long id,long tenantId,String platformCode,String endpointUrl,String credentialKey,String credentialSecret){}
    record PreparedReport(String endpointUrl,byte[] body,Map<String,String> headers){}
}
