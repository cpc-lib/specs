package com.example.evcharging.framework.webmvc.resilience;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.*;
import com.alibaba.csp.sentinel.slots.block.flow.*;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class SentinelHotPathRuleBootstrap implements InitializingBean {
    private final double chargingStartQps;
    private final double chargingStopQps;
    private final double paymentCreateQps;
    private final double refundCreateQps;

    public SentinelHotPathRuleBootstrap(
            @Value("${charging.resilience.hot-path.charging-start-qps:300}") double chargingStartQps,
            @Value("${charging.resilience.hot-path.charging-stop-qps:500}") double chargingStopQps,
            @Value("${charging.resilience.hot-path.payment-create-qps:300}") double paymentCreateQps,
            @Value("${charging.resilience.hot-path.refund-create-qps:100}") double refundCreateQps){
        this.chargingStartQps=chargingStartQps;this.chargingStopQps=chargingStopQps;
        this.paymentCreateQps=paymentCreateQps;this.refundCreateQps=refundCreateQps;
    }

    @Override public void afterPropertiesSet(){
        FlowRuleManager.loadRules(List.of(
                qps("charging.start",chargingStartQps),
                qps("charging.stop",chargingStopQps),
                qps("payment.create",paymentCreateQps),
                qps("payment.refund",refundCreateQps)
        ));
        DegradeRuleManager.loadRules(List.of(
                errorRatio("charging.start"),
                errorRatio("payment.create")
        ));
    }

    private FlowRule qps(String resource,double count){
        FlowRule r=new FlowRule(resource);
        r.setGrade(RuleConstant.FLOW_GRADE_QPS);
        r.setCount(count);
        r.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_DEFAULT);
        return r;
    }

    private DegradeRule errorRatio(String resource){
        DegradeRule r=new DegradeRule(resource);
        r.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO);
        r.setCount(0.5);
        r.setTimeWindow(10);
        r.setStatIntervalMs(10_000);
        r.setMinRequestAmount(20);
        return r;
    }
}
