package com.example.evcharging.system.auth;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.util.*;

public final class PasswordHasher {
    private static final SecureRandom RANDOM=new SecureRandom();
    private static final int ITERATIONS=120_000;
    private static final int KEY_LENGTH=256;

    private PasswordHasher(){}

    public static String hash(char[] password){
        byte[] salt=new byte[16];RANDOM.nextBytes(salt);
        byte[] hash=derive(password,salt,ITERATIONS);
        return "pbkdf2$"+ITERATIONS+"$"+Base64.getEncoder().encodeToString(salt)+"$"+Base64.getEncoder().encodeToString(hash);
    }

    public static boolean verify(char[] password,String encoded){
        try{
            String[] p=encoded.split("\\$");
            if(p.length!=4||!"pbkdf2".equals(p[0])) return false;
            int iterations=Integer.parseInt(p[1]);
            byte[] salt=Base64.getDecoder().decode(p[2]);
            byte[] expected=Base64.getDecoder().decode(p[3]);
            return java.security.MessageDigest.isEqual(expected,derive(password,salt,iterations));
        }catch(Exception e){return false;}
    }

    private static byte[] derive(char[] password,byte[] salt,int iterations){
        try{
            PBEKeySpec spec=new PBEKeySpec(password,salt,iterations,KEY_LENGTH);
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        }catch(Exception e){throw new IllegalStateException("cannot hash password",e);}
    }
}
