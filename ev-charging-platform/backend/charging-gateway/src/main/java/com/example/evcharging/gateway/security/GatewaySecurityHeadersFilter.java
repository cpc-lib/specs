package com.example.evcharging.gateway.security;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.*;
import reactor.core.publisher.Mono;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE+50)
public class GatewaySecurityHeadersFilter implements WebFilter {
    @Override public Mono<Void> filter(ServerWebExchange exchange,WebFilterChain chain){
        HttpHeaders h=exchange.getResponse().getHeaders();
        h.set("X-Content-Type-Options","nosniff");
        h.set("X-Frame-Options","DENY");
        h.set("Referrer-Policy","no-referrer");
        h.set("Permissions-Policy","camera=(), microphone=(), geolocation=()");
        return chain.filter(exchange);
    }
}
