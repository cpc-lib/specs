package com.example.evcharging.system.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.Base64;

public final class RefreshTokenHasher {
    private static final SecureRandom RANDOM=new SecureRandom();
    private RefreshTokenHasher(){}

    public static String newToken(){
        byte[] bytes=new byte[32];RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static String hash(String token){
        if(token==null||token.isBlank()) throw new IllegalArgumentException("refresh token required");
        try{
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8)));
        }catch(Exception e){throw new IllegalStateException("cannot hash refresh token",e);}
    }
}
