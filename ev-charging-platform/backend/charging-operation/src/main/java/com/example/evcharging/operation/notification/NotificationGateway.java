package com.example.evcharging.operation.notification;

public interface NotificationGateway {
    boolean supports(String channel);
    SendResult send(NotificationMessage message);

    record NotificationMessage(
            String taskNo,
            String channel,
            String recipient,
            String content) {}

    record SendResult(boolean success, String providerMessageId, String errorMessage) {
        public static SendResult ok(String providerMessageId) {
            return new SendResult(true, providerMessageId, null);
        }
        public static SendResult failed(String error) {
            return new SendResult(false, null, error);
        }
    }
}
