package com.example.evcharging.framework.webmvc.resilience;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class BoundedExecutorConfiguration {
    @Bean("ioBoundedExecutor")
    TaskExecutor ioBoundedExecutor(
            @Value("${charging.executor.io.core-size:8}") int core,
            @Value("${charging.executor.io.max-size:32}") int max,
            @Value("${charging.executor.io.queue-capacity:500}") int queue){
        return executor("ev-io-",core,max,queue);
    }

    @Bean("businessBoundedExecutor")
    TaskExecutor businessBoundedExecutor(
            @Value("${charging.executor.business.core-size:8}") int core,
            @Value("${charging.executor.business.max-size:24}") int max,
            @Value("${charging.executor.business.queue-capacity:1000}") int queue){
        return executor("ev-biz-",core,max,queue);
    }

    private ThreadPoolTaskExecutor executor(String prefix,int core,int max,int queue){
        if(core<=0||max<core||queue<0)throw new IllegalArgumentException("invalid bounded executor configuration");
        ThreadPoolTaskExecutor e=new ThreadPoolTaskExecutor();
        e.setThreadNamePrefix(prefix);
        e.setCorePoolSize(core);
        e.setMaxPoolSize(max);
        e.setQueueCapacity(queue);
        e.setKeepAliveSeconds(60);
        e.setWaitForTasksToCompleteOnShutdown(true);
        e.setAwaitTerminationSeconds(30);
        e.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.AbortPolicy());
        return e;
    }
}
