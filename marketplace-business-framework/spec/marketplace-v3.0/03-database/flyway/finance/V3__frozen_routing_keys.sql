ALTER TABLE payment_clearing_allocation ADD COLUMN payment_no VARCHAR(64) NULL AFTER clearing_record_id;
UPDATE payment_clearing_allocation a
JOIN payment_clearing_record r ON r.id = a.clearing_record_id
SET a.payment_no = r.payment_no
WHERE a.payment_no IS NULL;
ALTER TABLE payment_clearing_allocation
  MODIFY payment_no VARCHAR(64) NOT NULL,
  ADD KEY idx_clearing_allocation_payment_route (payment_no, merchant_id, merchant_order_id);
