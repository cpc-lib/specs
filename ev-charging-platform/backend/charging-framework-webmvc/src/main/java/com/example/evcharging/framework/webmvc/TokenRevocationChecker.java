package com.example.evcharging.framework.webmvc;

import com.example.evcharging.framework.security.VerifiedAccessToken;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class TokenRevocationChecker {
    private final StringRedisTemplate redis;
    public TokenRevocationChecker(StringRedisTemplate redis){this.redis=redis;}

    public void requireActive(VerifiedAccessToken token){
        try{
            if(Boolean.TRUE.equals(redis.hasKey(sessionKey(token.sessionId()))))
                throw new SecurityException("access session revoked");
            if(Boolean.TRUE.equals(redis.hasKey(tokenKey(token.tokenId()))))
                throw new SecurityException("access token revoked");
        }catch(SecurityException e){throw e;}
        catch(Exception infrastructure){
            // Authentication must fail closed: a revocation outage may never become an authorization bypass.
            throw new SecurityException("token revocation store unavailable");
        }
    }

    public static String sessionKey(String sessionId){return "ev:auth:revoked-session:"+sessionId;}
    public static String tokenKey(String tokenId){return "ev:auth:revoked-token:"+tokenId;}
}
