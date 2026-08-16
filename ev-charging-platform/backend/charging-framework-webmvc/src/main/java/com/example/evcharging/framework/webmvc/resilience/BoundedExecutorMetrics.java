package com.example.evcharging.framework.webmvc.resilience;

import io.micrometer.core.instrument.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@Component
public class BoundedExecutorMetrics {
    public BoundedExecutorMetrics(
            MeterRegistry registry,
            @Qualifier("ioBoundedExecutor") org.springframework.core.task.TaskExecutor io,
            @Qualifier("businessBoundedExecutor") org.springframework.core.task.TaskExecutor business){
        bind(registry,"io",(ThreadPoolTaskExecutor)io);
        bind(registry,"business",(ThreadPoolTaskExecutor)business);
    }

    private void bind(MeterRegistry registry,String name,ThreadPoolTaskExecutor e){
        Gauge.builder("ev.executor.active",e,ThreadPoolTaskExecutor::getActiveCount)
                .tag("executor",name).register(registry);
        Gauge.builder("ev.executor.pool.size",e,ThreadPoolTaskExecutor::getPoolSize)
                .tag("executor",name).register(registry);
        Gauge.builder("ev.executor.queue.size",e,
                x->x.getThreadPoolExecutor()==null?0:x.getThreadPoolExecutor().getQueue().size())
                .tag("executor",name).register(registry);
        Gauge.builder("ev.executor.queue.remaining",e,
                x->x.getThreadPoolExecutor()==null?0:x.getThreadPoolExecutor().getQueue().remainingCapacity())
                .tag("executor",name).register(registry);
    }
}
