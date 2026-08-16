package com.company.marketplace.gateway;
import org.springframework.cloud.gateway.filter.*; import org.springframework.core.Ordered; import org.springframework.stereotype.Component; import reactor.core.publisher.Mono;
@Component public final class InternalHeaderSanitizerFilter implements GlobalFilter, Ordered {
 private static final String[] UNTRUSTED={"X-Internal-User-Id","X-Internal-Merchant-Id","X-Internal-Shop-Id","X-Internal-Data-Scope"};
 public Mono<Void> filter(org.springframework.web.server.ServerWebExchange exchange, GatewayFilterChain chain){ var b=exchange.getRequest().mutate(); for(String h:UNTRUSTED)b.headers(x->x.remove(h)); return chain.filter(exchange.mutate().request(b.build()).build()); }
 public int getOrder(){return Ordered.HIGHEST_PRECEDENCE;}
}
