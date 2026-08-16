# Buyer-Seller IM SPEC

## Conversation
Participants:
buyer
merchant/shop agents
optional platform support participant.

Conversation context can reference:
offer
merchantOrder
afterSale
dispute.

## Message
serverMessageId
conversationId
sender
clientMessageId
contentType
text/contentRef
attachmentFileIds
moderationStatus
deliveryStatus
createdAt.

Content types:
TEXT
IMAGE
VIDEO
FILE
PRODUCT_CARD
ORDER_CARD
AFTERSALE_CARD
SYSTEM_EVENT.

Unique:
conversation + sender + clientMessageId.

## Delivery
ACCEPTED -> DELIVERED -> READ
May BLOCKED_BY_MODERATION.

## Security
No plaintext payment passwords/codes.
Attachments use File Service permissions and malware/content scan.
Message moderation/audit retention policy applies.
