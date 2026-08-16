package com.example.evcharging.open.callback;

import com.example.evcharging.open.security.OpenApiSignature;
import java.util.UUID;

public final class PartnerCallbackSigner {
    private PartnerCallbackSigner(){}

    public static SignedCallback sign(String secret,byte[] body,long epochSecond){
        String nonce=UUID.randomUUID().toString();
        String bodyHash=OpenApiSignature.sha256Hex(body);
        String canonical=epochSecond+"\n"+nonce+"\n"+bodyHash;
        String signature=OpenApiSignature.signHex(secret,canonical);
        return new SignedCallback(String.valueOf(epochSecond),nonce,bodyHash,signature);
    }

    public record SignedCallback(String timestamp,String nonce,String bodySha256,String signature){}
}
