package com.example.evcharging.framework.security;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.Base64;

public final class AccessTokenCodec {
    private static final Base64.Encoder ENCODER=Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODER=Base64.getUrlDecoder();

    private final ObjectMapper mapper;
    private final byte[] secret;

    public AccessTokenCodec(ObjectMapper mapper,String secret){
        this.mapper=Objects.requireNonNull(mapper,"mapper");
        if(secret==null||secret.length()<32) throw new IllegalArgumentException("access token secret must be >= 32 chars");
        this.secret=secret.getBytes(StandardCharsets.UTF_8);
    }

    public String issue(AccessPrincipal principal,Instant expiresAt){
        return issue(principal,expiresAt,UUID.randomUUID().toString(),UUID.randomUUID().toString()).token();
    }

    public IssuedAccessToken issue(AccessPrincipal principal,Instant expiresAt,String sessionId){
        return issue(principal,expiresAt,sessionId,UUID.randomUUID().toString());
    }

    public IssuedAccessToken issue(AccessPrincipal principal,Instant expiresAt,String sessionId,String tokenId){
        if(sessionId==null||sessionId.isBlank()) throw new IllegalArgumentException("sessionId required");
        if(tokenId==null||tokenId.isBlank()) throw new IllegalArgumentException("tokenId required");
        try{
            Map<String,Object> payload=new LinkedHashMap<>();
            payload.put("typ","access");
            payload.put("jti",tokenId);
            payload.put("sid",sessionId);
            payload.put("tenantId",principal.tenantId());
            payload.put("userId",principal.userId());
            payload.put("username",principal.username());
            payload.put("roles",principal.roles());
            payload.put("permissions",principal.permissions());
            payload.put("dataScopeType",principal.dataScopeType().name());
            payload.put("stationIds",principal.stationIds());
            payload.put("exp",expiresAt.getEpochSecond());
            String body=ENCODER.encodeToString(mapper.writeValueAsBytes(payload));
            String token=body+"."+ENCODER.encodeToString(hmac(body));
            return new IssuedAccessToken(token,tokenId,sessionId,expiresAt);
        }catch(Exception e){
            throw new IllegalStateException("cannot issue access token",e);
        }
    }

    public AccessPrincipal verify(String token){
        return verifyToken(token).principal();
    }

    @SuppressWarnings("unchecked")
    public VerifiedAccessToken verifyToken(String token){
        try{
            if(token==null||token.isBlank()) throw new SecurityException("access token missing");
            String[] parts=token.split("\\.",-1);
            if(parts.length!=2) throw new SecurityException("invalid access token");
            byte[] expected=hmac(parts[0]),actual=DECODER.decode(parts[1]);
            if(!java.security.MessageDigest.isEqual(expected,actual)) throw new SecurityException("invalid access token signature");
            Map<String,Object> payload=mapper.readValue(DECODER.decode(parts[0]),Map.class);
            if(!"access".equals(payload.get("typ"))) throw new SecurityException("invalid token type");
            Instant expiresAt=Instant.ofEpochSecond(((Number)payload.get("exp")).longValue());
            if(!Instant.now().isBefore(expiresAt)) throw new SecurityException("access token expired");
            String tokenId=required(payload,"jti"),sessionId=required(payload,"sid");
            AccessPrincipal principal=new AccessPrincipal(
                    ((Number)payload.get("tenantId")).longValue(),
                    ((Number)payload.get("userId")).longValue(),
                    String.valueOf(payload.get("username")),
                    stringSet((Collection<?>)payload.get("roles")),
                    stringSet((Collection<?>)payload.get("permissions")),
                    DataScopeType.valueOf(String.valueOf(payload.get("dataScopeType"))),
                    longSet((Collection<?>)payload.get("stationIds")));
            return new VerifiedAccessToken(principal,tokenId,sessionId,expiresAt);
        }catch(SecurityException e){throw e;}
        catch(Exception e){throw new SecurityException("invalid access token",e);}
    }

    private String required(Map<String,Object> payload,String name){
        Object value=payload.get(name);
        if(value==null||String.valueOf(value).isBlank()) throw new SecurityException("missing token claim: "+name);
        return String.valueOf(value);
    }
    private byte[] hmac(String body)throws Exception{
        Mac mac=Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret,"HmacSHA256"));
        return mac.doFinal(body.getBytes(StandardCharsets.UTF_8));
    }
    private static Set<String> stringSet(Collection<?> values){
        if(values==null)return Set.of();Set<String> out=new LinkedHashSet<>();
        for(Object v:values)out.add(String.valueOf(v));return out;
    }
    private static Set<Long> longSet(Collection<?> values){
        if(values==null)return Set.of();Set<Long> out=new LinkedHashSet<>();
        for(Object v:values)out.add(((Number)v).longValue());return out;
    }

    public record IssuedAccessToken(String token,String tokenId,String sessionId,Instant expiresAt){}
}
