package com.example.evcharging.open.security;

import org.junit.jupiter.api.Test;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;

class OpenApiSignatureTest {
    @Test void canonicalQueryIsSortedAndBodyHashIsStable(){
        String canonical=OpenApiSignature.canonical("post","/open-api/v1/charging/start",
                "z=2&a=hello%20world&a=alpha","{}".getBytes(StandardCharsets.UTF_8),"1700000000","nonce-1");
        assertTrue(canonical.contains("a=alpha&a=hello%20world&z=2"));
        assertEquals(OpenApiSignature.signHex("secret",canonical),OpenApiSignature.signHex("secret",canonical));
    }

    @Test void constantTimeComparisonIsCaseInsensitiveHex(){
        assertTrue(OpenApiSignature.constantTimeEquals("abc123","ABC123"));
        assertFalse(OpenApiSignature.constantTimeEquals("abc123","abc124"));
    }
}
