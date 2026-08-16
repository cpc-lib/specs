-- V3.0 payment/refund family routes by payment_no so callbacks and child facts can
-- be routed without broadcast.

ALTER TABLE payment_attempt ADD COLUMN payment_no VARCHAR(64) NULL AFTER payment_order_id;
UPDATE payment_attempt a
JOIN payment_order p ON p.id = a.payment_order_id
SET a.payment_no = p.payment_no
WHERE a.payment_no IS NULL;
ALTER TABLE payment_attempt
  MODIFY payment_no VARCHAR(64) NOT NULL,
  ADD KEY idx_payment_attempt_route (payment_no, status, id);

ALTER TABLE payment_transaction ADD COLUMN payment_no VARCHAR(64) NULL AFTER payment_order_id;
UPDATE payment_transaction tx
JOIN payment_order p ON p.id = tx.payment_order_id
SET tx.payment_no = p.payment_no
WHERE tx.payment_no IS NULL;
ALTER TABLE payment_transaction
  MODIFY payment_no VARCHAR(64) NOT NULL,
  ADD KEY idx_payment_transaction_route (payment_no, id);

ALTER TABLE refund_order ADD COLUMN payment_no VARCHAR(64) NULL AFTER refund_no;
UPDATE refund_order r
JOIN payment_order p ON p.id = r.payment_order_id
SET r.payment_no = p.payment_no
WHERE r.payment_no IS NULL;
ALTER TABLE refund_order
  MODIFY payment_no VARCHAR(64) NOT NULL,
  ADD KEY idx_refund_order_payment_route (payment_no, status, id);

ALTER TABLE refund_quota_reservation ADD COLUMN payment_no VARCHAR(64) NULL AFTER payment_order_id;
UPDATE refund_quota_reservation q
JOIN payment_order p ON p.id = q.payment_order_id
SET q.payment_no = p.payment_no
WHERE q.payment_no IS NULL;
ALTER TABLE refund_quota_reservation
  MODIFY payment_no VARCHAR(64) NOT NULL,
  ADD KEY idx_refund_quota_payment_route (payment_no, status, id);

ALTER TABLE refund_transaction ADD COLUMN payment_no VARCHAR(64) NULL AFTER refund_order_id;
UPDATE refund_transaction rt
JOIN refund_order r ON r.id = rt.refund_order_id
SET rt.payment_no = r.payment_no
WHERE rt.payment_no IS NULL;
ALTER TABLE refund_transaction
  MODIFY payment_no VARCHAR(64) NOT NULL,
  ADD KEY idx_refund_transaction_payment_route (payment_no, id);
