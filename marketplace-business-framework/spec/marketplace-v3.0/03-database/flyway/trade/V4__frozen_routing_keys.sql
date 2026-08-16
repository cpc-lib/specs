-- V3.0 routing-key hardening for the Trade binding-table family.
-- This repository is a greenfield codegen baseline. For a live large table,
-- execute add/backfill/validate/not-null as expand/backfill/contract deployment phases.

ALTER TABLE order_item ADD COLUMN buyer_id BIGINT NULL AFTER id;
UPDATE order_item oi
JOIN merchant_order mo ON mo.id = oi.merchant_order_id
SET oi.buyer_id = mo.buyer_id
WHERE oi.buyer_id IS NULL;
ALTER TABLE order_item
  MODIFY buyer_id BIGINT NOT NULL,
  ADD KEY idx_order_item_buyer_route (buyer_id, merchant_order_id, id);

ALTER TABLE discount_allocation ADD COLUMN buyer_id BIGINT NULL AFTER id;
UPDATE discount_allocation da
JOIN trade t ON t.id = da.trade_id
SET da.buyer_id = t.buyer_id
WHERE da.buyer_id IS NULL;
ALTER TABLE discount_allocation
  MODIFY buyer_id BIGINT NOT NULL,
  ADD KEY idx_discount_allocation_buyer_route (buyer_id, trade_id, order_item_id);

ALTER TABLE order_item_economics_snapshot ADD COLUMN buyer_id BIGINT NULL AFTER id;
UPDATE order_item_economics_snapshot s
JOIN trade t ON t.id = s.trade_id
SET s.buyer_id = t.buyer_id
WHERE s.buyer_id IS NULL;
ALTER TABLE order_item_economics_snapshot
  MODIFY buyer_id BIGINT NOT NULL,
  ADD KEY idx_order_economics_buyer_route (buyer_id, trade_id, order_item_id);

ALTER TABLE funding_allocation ADD COLUMN buyer_id BIGINT NULL AFTER id;
UPDATE funding_allocation f
JOIN trade t ON t.id = f.trade_id
SET f.buyer_id = t.buyer_id
WHERE f.buyer_id IS NULL;
ALTER TABLE funding_allocation
  MODIFY buyer_id BIGINT NOT NULL,
  ADD KEY idx_funding_allocation_buyer_route (buyer_id, trade_id, order_item_id);
