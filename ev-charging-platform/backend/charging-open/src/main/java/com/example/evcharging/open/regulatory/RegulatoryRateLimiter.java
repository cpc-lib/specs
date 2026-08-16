package com.example.evcharging.open.regulatory;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RegulatoryRateLimiter {
    private static final DefaultRedisScript<Long> SCRIPT=new DefaultRedisScript<>("""
        local n=redis.call('INCR',KEYS[1])
        if n==1 then redis.call('EXPIRE',KEYS[1],ARGV[1]) end
        return n
        """,Long.class);
    private final StringRedisTemplate redis;
    public RegulatoryRateLimiter(StringRedisTemplate redis){this.redis=redis;}

    public void require(long platformId,int limit){
        long minute=System.currentTimeMillis()/60000L;
        Long n=redis.execute(SCRIPT,List.of("ev:regulatory:rate:"+platformId+":"+minute),"120");
        if(n==null)throw new IllegalStateException("regulatory rate limiter unavailable");
        if(n>Math.max(1,limit))throw new IllegalStateException("regulatory rate limit exceeded");
    }
}
