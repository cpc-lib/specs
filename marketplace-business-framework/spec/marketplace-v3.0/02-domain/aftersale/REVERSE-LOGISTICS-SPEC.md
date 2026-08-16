# Reverse Logistics SPEC

ReverseShipment:
- reverseShipmentNo
- returnOrderId
- carrierCode
- trackingNo
- pickupMode: BUYER_SHIP / COURIER_PICKUP / MERCHANT_PICKUP
- status
- receivedAt

States:
CREATED -> WAITING_PICKUP -> IN_TRANSIT -> DELIVERED_TO_RETURN_NODE -> RECEIVED
Branches: LOST / DELIVERY_FAILED / RETURNED_TO_BUYER / EXCEPTION.

Tracking events are append-only and normalized exactly like forward Shipment.

Return addresses are snapshotted. Seller changing return address later must not affect an already approved ReturnOrder.
