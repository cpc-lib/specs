# Notification Deepening SPEC

## Aggregates
NotificationPreference
NotificationTemplateVersion
NotificationMessage
NotificationDelivery
NotificationDeliveryAttempt
NotificationSuppression

Channels:
IN_APP
PUSH
SMS
EMAIL
WECHAT

Business services publish intent/events.
Notification owns delivery.

## Categories
TRANSACTIONAL
OPERATIONAL
LEGAL
MARKETING

Marketing opt-out applies to marketing only.
Quiet hours and channel preference are policy-controlled.

## Delivery
PENDING -> SENDING -> SENT -> DELIVERED
Branches:
RETRY_WAIT / FAILED / BOUNCED / REJECTED / SUPPRESSED.

SENT means provider accepted.
Only receipt-capable provider can prove DELIVERED.

Dedup:
businessType + businessId + template/rule + recipientRef + triggerKey.
