# Fulfillment Deepening SPEC V2.3

## 1. Aggregates
### FulfillmentOrder
One responsibility plan for a MerchantOrder or portion of it.

Fields:
- fulfillmentNo
- merchantOrderId
- merchantId/shopId
- fulfillmentType: PHYSICAL / DIGITAL / SERVICE
- fulfillmentMode: MERCHANT / PLATFORM / THIRD_PARTY
- warehouseId nullable
- routeSnapshotId
- promisedShipAt / promisedDeliverAt
- status
- version

States:
CREATED -> ALLOCATED -> PICKING -> PACKING -> PARTIALLY_SHIPPED / SHIPPED
CREATED/ALLOCATED -> CANCELLED
SHIPPED -> DELIVERED -> RECEIVED -> COMPLETED
Exception branches use EXCEPTION but must preserve package/shipment facts.

### FulfillmentOrderItem
Tracks item quantity:
- orderItemId
- skuId
- orderedQty
- fulfillQty
- cancelledQty
- returnedQty
- exchangedQty

Invariant:
quantities can never exceed original OrderItem quantity.

### Package
A package may contain portions of multiple OrderItems.
Package status:
CREATED -> PACKED -> HANDED_OVER -> IN_TRANSIT -> DELIVERED -> RECEIVED -> CLOSED
May enter EXCEPTION / RETURNING.

### PackageItem
(orderItemId, quantity) mapping. This is mandatory.

### Shipment
Carrier fact:
CREATED -> LABEL_CREATED -> PICKED_UP -> IN_TRANSIT -> DELIVERED
or DELIVERY_FAILED / RETURNING / RETURNED / LOST.

One Package can have multiple shipment legs if carrier handoff/reship policy requires it.

## 2. Partial Shipment
MerchantOrder with item A qty2, item B qty1 may ship:
Package P1: A qty1
Package P2 later: A qty1 + B qty1.

MerchantOrder summary:
WAITING_FULFILLMENT -> PARTIALLY_SHIPPED -> SHIPPED.
Never mark SHIPPED until all non-cancelled fulfillable quantity has left origin.

## 3. Idempotency
- CreateFulfillment: MerchantOrder + fulfillmentPlanVersion
- CreatePackage: fulfillmentNo + clientPackageRequestId
- ShipPackage: packageNo + shipmentRequestId
- CarrierTrackingCallback: carrier + trackingNo + providerEventId
- ConfirmReceive: packageNo/orderNo + command id

## 4. Overship Prevention
Before package shipment:
lock FulfillmentOrderItem rows in stable id order.
Require:
alreadyPackedOrShippedQty + newQty <= fulfillableQty.

## 5. Completion
MerchantOrder completion is a policy decision after:
- all required quantities received/fulfilled
- no blocking open aftersale for completion scope
- auto-receive or buyer receive conditions satisfied
It is not derived solely from carrier DELIVERED.
