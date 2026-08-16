package com.example.evcharging.operation.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MockNotificationGateway implements NotificationGateway {
    private static final Logger log=LoggerFactory.getLogger(MockNotificationGateway.class);

    @Override public boolean supports(String channel) {
        return "APP".equalsIgnoreCase(channel)
                || "SMS".equalsIgnoreCase(channel)
                || "WECHAT".equalsIgnoreCase(channel);
    }

    @Override public SendResult send(NotificationMessage message) {
        log.info("MOCK_NOTIFICATION task={} channel={} recipient={} content={}",
                message.taskNo(),message.channel(),message.recipient(),message.content());
        return SendResult.ok("MOCK-"+message.taskNo());
    }
}
