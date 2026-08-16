package com.example.evcharging.iot.messaging;

import com.example.evcharging.framework.contract.DeviceCommandMessage;
import com.example.evcharging.iot.gateway.DeviceChannelRegistry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Configuration
class DeviceCommandRabbitConfig {
    static final String DLX = "ev.device.command.dlx";
    static final String DLQ = "ev.device.command.dlq";
    static final String DLQ_ROUTING = "device.command.dead";

    @Bean
    DirectExchange deviceCommandExchange(@Value("${iot.command.exchange:ev.device.command.exchange}") String exchange) {
        return new DirectExchange(exchange, true, false);
    }

    @Bean
    DirectExchange deviceCommandDeadLetterExchange() {
        return new DirectExchange(DLX, true, false);
    }

    @Bean
    Queue deviceCommandQueue(@Value("${iot.gateway-id:dev}") String gatewayId) {
        return QueueBuilder.durable("ev.device.command.gateway-" + gatewayId)
                .deadLetterExchange(DLX)
                .deadLetterRoutingKey(DLQ_ROUTING)
                .build();
    }

    @Bean
    Queue deviceCommandDeadLetterQueue() {
        return QueueBuilder.durable(DLQ).build();
    }

    @Bean
    Binding deviceCommandBinding(@Qualifier("deviceCommandQueue") Queue deviceCommandQueue,
                                 @Qualifier("deviceCommandExchange") DirectExchange deviceCommandExchange,
                                 @Value("${iot.gateway-id:dev}") String gatewayId) {
        return BindingBuilder.bind(deviceCommandQueue).to(deviceCommandExchange).with("gateway." + gatewayId);
    }

    @Bean
    Binding deviceCommandDeadLetterBinding(@Qualifier("deviceCommandDeadLetterQueue") Queue deviceCommandDeadLetterQueue,
                                           @Qualifier("deviceCommandDeadLetterExchange") DirectExchange deviceCommandDeadLetterExchange) {
        return BindingBuilder.bind(deviceCommandDeadLetterQueue).to(deviceCommandDeadLetterExchange).with(DLQ_ROUTING);
    }
}

@Component
public class DeviceCommandMessaging {
    private static final Logger log = LoggerFactory.getLogger(DeviceCommandMessaging.class);
    private final ObjectMapper objectMapper;
    private final DeviceChannelRegistry registry;

    public DeviceCommandMessaging(ObjectMapper objectMapper, DeviceChannelRegistry registry) {
        this.objectMapper = objectMapper;
        this.registry = registry;
    }

    @RabbitListener(queues = "#{deviceCommandQueue.name}")
    public void receive(String message) throws JsonProcessingException {
        DeviceCommandMessage command = objectMapper.readValue(message, DeviceCommandMessage.class);
        if (command.expireAt() != null && command.expireAt().isBefore(Instant.now())) {
            throw new IllegalStateException("expired device command: " + command.commandId());
        }
        Channel channel = registry.find(command.tenantId(), command.deviceId())
                .orElseThrow(() -> new IllegalStateException("device offline: " + command.deviceId()));
        String payload = objectMapper.writeValueAsString(command.payload());
        channel.writeAndFlush("COMMAND|" + command.commandId() + "|" + command.commandType() + "|" + payload + "\n");
        log.info("device command delivered tenant={} device={} commandId={} type={}",
                command.tenantId(), command.deviceId(), command.commandId(), command.commandType());
    }
}
