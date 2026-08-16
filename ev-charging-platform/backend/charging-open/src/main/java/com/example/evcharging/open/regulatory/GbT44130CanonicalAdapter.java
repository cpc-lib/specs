package com.example.evcharging.open.regulatory;

import com.example.evcharging.open.callback.PartnerCallbackSigner;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

@Component
public class GbT44130CanonicalAdapter implements RegulatoryProtocolAdapter {
    public static final String PROTOCOL="GB_T_44130_2025_CANONICAL";
    private final ObjectMapper mapper;

    public GbT44130CanonicalAdapter(ObjectMapper mapper){this.mapper=mapper;}

    @Override public boolean supports(String protocolCode){return PROTOCOL.equalsIgnoreCase(protocolCode);}

    @Override
    public PreparedReport prepare(Platform platform,String dataType,String businessKey,String sourcePayloadJson){
        try{
            String part=switch(dataType){
                case "PUBLIC_STATION" -> "GB/T 44130.2-2025";
                case "BUSINESS_ORDER" -> "GB/T 44130.3-2025";
                default -> throw new IllegalArgumentException("unsupported GB/T 44130 canonical data type: "+dataType);
            };
            Map<String,Object> envelope=new LinkedHashMap<>();
            envelope.put("standardFamily","GB/T 44130");
            envelope.put("standardPart",part);
            envelope.put("profile","canonical-adapter-not-platform-certified");
            envelope.put("platformCode",platform.platformCode());
            envelope.put("dataType",dataType);
            envelope.put("businessKey",businessKey);
            envelope.put("generatedAt",Instant.now().toString());
            envelope.put("payload",mapper.readTree(sourcePayloadJson));
            byte[] body=mapper.writeValueAsBytes(envelope);

            Map<String,String> headers=new LinkedHashMap<>();
            headers.put("Content-Type","application/json");
            if(platform.credentialKey()!=null&&!platform.credentialKey().isBlank())headers.put("X-Platform-Key",platform.credentialKey());
            if(platform.credentialSecret()!=null&&!platform.credentialSecret().isBlank()){
                long epoch=Instant.now().getEpochSecond();
                var signed=PartnerCallbackSigner.sign(platform.credentialSecret(),body,epoch);
                headers.put("X-Timestamp",signed.timestamp());
                headers.put("X-Nonce",signed.nonce());
                headers.put("X-Body-SHA256",signed.bodySha256());
                headers.put("X-Signature-Version","v1");
                headers.put("X-Signature",signed.signature());
            }
            return new PreparedReport(platform.endpointUrl(),body,headers);
        }catch(RuntimeException e){throw e;}
        catch(Exception e){throw new IllegalStateException("cannot prepare GB/T 44130 canonical report",e);}
    }
}
