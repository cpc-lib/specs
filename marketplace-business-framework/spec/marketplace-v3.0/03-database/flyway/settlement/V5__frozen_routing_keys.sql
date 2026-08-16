-- Settlement binding family routes by merchant_id.

ALTER TABLE settlement_item ADD COLUMN merchant_id BIGINT NULL AFTER settlement_batch_id;
UPDATE settlement_item i
JOIN settlement_batch b ON b.id = i.settlement_batch_id
SET i.merchant_id = b.merchant_id
WHERE i.merchant_id IS NULL;
ALTER TABLE settlement_item
  MODIFY merchant_id BIGINT NOT NULL,
  ADD KEY idx_settlement_item_merchant_route (merchant_id, settlement_batch_id, id);

ALTER TABLE payout_reservation ADD COLUMN merchant_id BIGINT NULL AFTER merchant_payable_id;
UPDATE payout_reservation r
JOIN merchant_payable p ON p.id = r.merchant_payable_id
SET r.merchant_id = p.merchant_id
WHERE r.merchant_id IS NULL;
ALTER TABLE payout_reservation
  MODIFY merchant_id BIGINT NOT NULL,
  ADD KEY idx_payout_reservation_merchant_route (merchant_id, merchant_payable_id, status);

ALTER TABLE payout_transaction ADD COLUMN merchant_id BIGINT NULL AFTER payout_order_id;
UPDATE payout_transaction tx
JOIN payout_order p ON p.id = tx.payout_order_id
SET tx.merchant_id = p.merchant_id
WHERE tx.merchant_id IS NULL;
ALTER TABLE payout_transaction
  MODIFY merchant_id BIGINT NOT NULL,
  ADD KEY idx_payout_transaction_merchant_route (merchant_id, payout_order_id, id);
