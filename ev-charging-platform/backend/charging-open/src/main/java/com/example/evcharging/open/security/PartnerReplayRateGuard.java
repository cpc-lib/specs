package com.example.evcharging.open.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Component
public class PartnerReplayRateGuard {
    private static final DefaultRedisScript<Long> RATE_SCRIPT=new DefaultRedisScript<>("""
        local n=redis.call('INCR',KEYS[1])
        if n==1 then redis.call('EXPIRE',KEYS[1],ARGV[1]) end
        return n
        """,Long.class);

    private final StringRedisTemplate redis;
    private final Duration nonceTtl;

    public PartnerReplayRateGuard(StringRedisTemplate redis,
            @Value("${charging.open.nonce-ttl-seconds:600}") long nonceTtlSeconds){
        this.redis=redis;this.nonceTtl=Duration.ofSeconds(nonceTtlSeconds);
    }

    public void requireFreshNonce(String appKey,String nonce){
        String key="ev:open:nonce:"+appKey+":"+nonce;
        Boolean accepted=redis.opsForValue().setIfAbsent(key,"1",nonceTtl);
        if(!Boolean.TRUE.equals(accepted))throw new SecurityException("replayed nonce");
    }

    public void requireWithinRate(String appKey,int limitPerMinute,long epochSecond){
        long minute=epochSecond/60;
        String key="ev:open:rate:"+appKey+":"+minute;
        Long count=redis.execute(RATE_SCRIPT,List.of(key),"120");
        if(count==null)throw new SecurityException("rate limiter unavailable");
        if(count>Math.max(1,limitPerMinute))throw new RateLimitException("partner rate limit exceeded");
    }

    public static final class RateLimitException extends SecurityException{
        public RateLimitException(String message){super(message);}
    }
}
