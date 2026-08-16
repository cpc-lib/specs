package com.example.evcharging.open.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;

@Component
public class SecretCipher {
    private static final SecureRandom RANDOM=new SecureRandom();
    private final String activeKeyId;
    private final byte[] activeKey;
    private final Map<String,byte[]> keyRing;

    public SecretCipher(
            @Value("${charging.open.master-key-id:${OPENAPI_MASTER_KEY_ID:primary}}") String activeKeyId,
            @Value("${charging.open.master-key-base64}") String activeKeyBase64,
            @Value("${charging.open.previous-master-keys:${OPENAPI_PREVIOUS_MASTER_KEYS:}}") String previous){
        this.activeKeyId=validateKeyId(activeKeyId);
        this.activeKey=decodeKey(activeKeyBase64);
        Map<String,byte[]> ring=new LinkedHashMap<>();ring.put(this.activeKeyId,this.activeKey);
        if(previous!=null&&!previous.isBlank()){
            for(String item:previous.split(",")){
                if(item.isBlank())continue;
                String[] pair=item.trim().split("=",2);
                if(pair.length!=2)throw new IllegalArgumentException("invalid OPENAPI_PREVIOUS_MASTER_KEYS entry");
                String id=validateKeyId(pair[0].trim());
                if(ring.putIfAbsent(id,decodeKey(pair[1].trim()))!=null)
                    throw new IllegalArgumentException("duplicate OpenAPI master key id: "+id);
            }
        }
        this.keyRing=Map.copyOf(ring);
    }

    public String encrypt(String plaintext){
        if(plaintext==null)return null;
        return "v2:"+activeKeyId+":"+encryptWith(activeKey,plaintext);
    }

    public String decrypt(String encoded){
        if(encoded==null)return null;
        if(encoded.startsWith("v2:")){
            String[] parts=encoded.split(":",3);
            if(parts.length!=3)throw new IllegalArgumentException("invalid v2 secret");
            byte[] key=keyRing.get(parts[1]);
            if(key==null)throw new SecurityException("secret key id unavailable: "+parts[1]);
            return decryptWith(key,parts[2]);
        }
        if(encoded.startsWith("v1:")){
            // Upgrade path from SPEC 8.2: v1 did not record a key ID, so decrypt with the current active key
            // before rotating the deployment key. Then call the rewrap endpoint to persist v2:keyId ciphertext.
            return decryptWith(activeKey,encoded.substring(3));
        }
        throw new IllegalArgumentException("unsupported secret cipher version");
    }

    public boolean usesActiveKey(String encoded){return encoded!=null&&encoded.startsWith("v2:"+activeKeyId+":");}
    public String rewrap(String encoded){if(encoded==null||usesActiveKey(encoded))return encoded;return encrypt(decrypt(encoded));}
    public String activeKeyId(){return activeKeyId;}

    private String encryptWith(byte[] key,String plaintext){
        try{
            byte[] iv=new byte[12];RANDOM.nextBytes(iv);
            Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE,new SecretKeySpec(key,"AES"),new GCMParameterSpec(128,iv));
            byte[] encrypted=cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            byte[] all=new byte[iv.length+encrypted.length];
            System.arraycopy(iv,0,all,0,iv.length);System.arraycopy(encrypted,0,all,iv.length,encrypted.length);
            return Base64.getEncoder().encodeToString(all);
        }catch(Exception e){throw new IllegalStateException("cannot encrypt secret",e);}
    }

    private String decryptWith(byte[] key,String payload){
        try{
            byte[] all=Base64.getDecoder().decode(payload);
            if(all.length<29)throw new IllegalArgumentException("invalid encrypted secret");
            byte[] iv=Arrays.copyOfRange(all,0,12),encrypted=Arrays.copyOfRange(all,12,all.length);
            Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE,new SecretKeySpec(key,"AES"),new GCMParameterSpec(128,iv));
            return new String(cipher.doFinal(encrypted),StandardCharsets.UTF_8);
        }catch(RuntimeException e){throw e;}
        catch(Exception e){throw new SecurityException("cannot decrypt secret",e);}
    }

    private static byte[] decodeKey(String base64){
        try{
            byte[] key=Base64.getDecoder().decode(base64);
            if(key.length!=32)throw new IllegalArgumentException("OpenAPI master key must be 32 bytes");
            return key;
        }catch(IllegalArgumentException e){throw e;}
        catch(Exception e){throw new IllegalArgumentException("invalid OpenAPI master key",e);}
    }
    private static String validateKeyId(String id){
        if(id==null||!id.matches("[A-Za-z0-9._-]{1,32}"))throw new IllegalArgumentException("invalid OpenAPI master key id");
        return id;
    }
}
