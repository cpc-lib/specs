package com.example.evcharging.gateway.resilience;

import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayRuleManager;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class GatewaySentinelRuleBootstrap implements InitializingBean {
    private final double adminQps;
    private final double appQps;
    private final double openQps;
    private final double internalQps;

    public GatewaySentinelRuleBootstrap(
            @Value("${charging.resilience.gateway.admin-qps:300}") double adminQps,
            @Value("${charging.resilience.gateway.app-qps:1000}") double appQps,
            @Value("${charging.resilience.gateway.open-qps:500}") double openQps,
            @Value("${charging.resilience.gateway.internal-qps:2000}") double internalQps){
        this.adminQps=adminQps;this.appQps=appQps;this.openQps=openQps;this.internalQps=internalQps;
    }

    @Override public void afterPropertiesSet(){
        Set<GatewayFlowRule> rules=new LinkedHashSet<>();
        rules.add(rule("system",adminQps));
        rules.add(rule("asset",appQps));
        rules.add(rule("core",appQps));
        rules.add(rule("payment",appQps));
        rules.add(rule("finance",adminQps));
        rules.add(rule("operation",adminQps));
        rules.add(rule("open",openQps));
        rules.add(rule("iot",internalQps));
        GatewayRuleManager.loadRules(rules);
    }

    private GatewayFlowRule rule(String routeId,double qps){
        return new GatewayFlowRule(routeId)
                .setCount(qps)
                .setIntervalSec(1);
    }
}
