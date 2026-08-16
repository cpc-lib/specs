-- Buyer conversations and messages bind on buyer_id.

ALTER TABLE im_message ADD COLUMN buyer_id BIGINT NULL AFTER conversation_id;
UPDATE im_message m
JOIN im_conversation c ON c.id = m.conversation_id
SET m.buyer_id = c.buyer_id
WHERE m.buyer_id IS NULL;
ALTER TABLE im_message
  MODIFY buyer_id BIGINT NOT NULL,
  ADD KEY idx_im_message_buyer_route (buyer_id, conversation_id, id);

ALTER TABLE im_read_cursor ADD COLUMN buyer_id BIGINT NULL AFTER conversation_id;
UPDATE im_read_cursor r
JOIN im_conversation c ON c.id = r.conversation_id
SET r.buyer_id = c.buyer_id
WHERE r.buyer_id IS NULL;
ALTER TABLE im_read_cursor
  MODIFY buyer_id BIGINT NOT NULL,
  ADD KEY idx_im_read_buyer_route (buyer_id, conversation_id, participant_id);
