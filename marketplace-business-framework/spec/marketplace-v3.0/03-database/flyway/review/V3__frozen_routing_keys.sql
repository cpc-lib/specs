-- Review facts route by offer_id to keep product-detail review reads local.

UPDATE review SET review_no = CONCAT('RVW-LEGACY-', id) WHERE review_no IS NULL;
ALTER TABLE review MODIFY review_no VARCHAR(64) NOT NULL;

ALTER TABLE additional_review ADD COLUMN offer_id BIGINT NULL AFTER review_id;
UPDATE additional_review a
JOIN review r ON r.id = a.review_id
SET a.offer_id = r.offer_id
WHERE a.offer_id IS NULL;
ALTER TABLE additional_review
  MODIFY offer_id BIGINT NOT NULL,
  ADD KEY idx_additional_review_offer_route (offer_id, review_id, id);

ALTER TABLE seller_review_reply ADD COLUMN offer_id BIGINT NULL AFTER review_id;
UPDATE seller_review_reply s
JOIN review r ON r.id = s.review_id
SET s.offer_id = r.offer_id
WHERE s.offer_id IS NULL;
ALTER TABLE seller_review_reply
  MODIFY offer_id BIGINT NOT NULL,
  ADD KEY idx_seller_review_reply_offer_route (offer_id, review_id, id);

ALTER TABLE review_moderation_record ADD COLUMN offer_id BIGINT NULL AFTER review_id;
UPDATE review_moderation_record m
JOIN review r ON r.id = m.review_id
SET m.offer_id = r.offer_id
WHERE m.offer_id IS NULL;
ALTER TABLE review_moderation_record
  MODIFY offer_id BIGINT NOT NULL,
  ADD KEY idx_review_moderation_offer_route (offer_id, review_id, id);
