package com.example.evcharging.open.security;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

public final class OpenApiSignature {
    private OpenApiSignature(){}

    public static String canonical(
            String method,String path,String rawQuery,byte[] body,String timestamp,String nonce){
        return method.toUpperCase(Locale.ROOT)+"\n"
                +path+"\n"
                +canonicalQuery(rawQuery)+"\n"
                +sha256Hex(body==null?new byte[0]:body)+"\n"
                +timestamp+"\n"
                +nonce;
    }

    public static String signHex(String secret,String canonical){
        try{
            Mac mac=Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),"HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(canonical.getBytes(StandardCharsets.UTF_8)));
        }catch(Exception e){throw new IllegalStateException("cannot sign openapi request",e);}
    }

    public static boolean constantTimeEquals(String expected,String actual){
        if(expected==null||actual==null)return false;
        return MessageDigest.isEqual(
                expected.toLowerCase(Locale.ROOT).getBytes(StandardCharsets.US_ASCII),
                actual.toLowerCase(Locale.ROOT).getBytes(StandardCharsets.US_ASCII));
    }

    public static String sha256Hex(byte[] bytes){
        try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));}
        catch(Exception e){throw new IllegalStateException("cannot hash request body",e);}
    }

    static String canonicalQuery(String rawQuery){
        if(rawQuery==null||rawQuery.isBlank())return "";
        List<Pair> pairs=new ArrayList<>();
        for(String item:rawQuery.split("&")){
            String[] kv=item.split("=",2);
            pairs.add(new Pair(decode(kv[0]),kv.length==2?decode(kv[1]):""));
        }
        pairs.sort(Comparator.comparing(Pair::key).thenComparing(Pair::value));
        StringJoiner out=new StringJoiner("&");
        for(Pair p:pairs)out.add(encode(p.key())+"="+encode(p.value()));
        return out.toString();
    }

    private static String decode(String v){
        try{return URLDecoder.decode(v,StandardCharsets.UTF_8);}
        catch(Exception e){throw new IllegalArgumentException("invalid query encoding",e);}
    }
    private static String encode(String v){
        return URLEncoder.encode(v,StandardCharsets.UTF_8).replace("+","%20")
                .replace("%7E","~");
    }
    private record Pair(String key,String value){}
}
