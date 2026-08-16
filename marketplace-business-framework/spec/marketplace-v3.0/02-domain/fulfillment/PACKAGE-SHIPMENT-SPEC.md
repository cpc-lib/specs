# Package / Shipment SPEC

## Package
Physical packing unit. Has immutable packageNo.
Must contain PackageItems with exact quantities.

## Shipment
Transport fact for a package/leg.
Fields:
carrierCode, serviceCode, trackingNo, shipFrom snapshot, shipTo snapshot,
weight, volume, shippingFee economic reference, status.

## Tracking Event
Append-only:
- providerEventId
- eventCode
- eventTime
- location
- rawStatus
- normalizedStatus
- rawPayloadRef

Duplicate provider event must be idempotent.
Out-of-order event cannot regress normalized state.

## Exceptions
- ADDRESS_PROBLEM
- DELIVERY_FAILED
- REFUSED
- LOST
- DAMAGED
- CUSTOMS_HOLD
- RETURN_TO_SENDER

Exception opens fulfillment operation task; it does not erase shipment history.
