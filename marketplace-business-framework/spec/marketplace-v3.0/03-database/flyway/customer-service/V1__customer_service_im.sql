CREATE TABLE customer_service_case (
  id BIGINT PRIMARY KEY,
  case_no VARCHAR(64) NOT NULL,
  requester_type VARCHAR(32) NOT NULL,
  requester_id BIGINT NOT NULL,
  buyer_id BIGINT NULL,
  merchant_id BIGINT NULL,
  shop_id BIGINT NULL,
  related_type VARCHAR(32) NULL,
  related_id VARCHAR(128) NULL,
  category VARCHAR(64) NOT NULL,
  priority VARCHAR(32) NOT NULL,
  status VARCHAR(32) NOT NULL,
  assigned_group VARCHAR(64) NULL,
  assigned_agent_id BIGINT NULL,
  sla_due_at DATETIME(3) NULL,
  resolution_code VARCHAR(64) NULL,
  resolution_note VARCHAR(2048) NULL,
  version INT NOT NULL DEFAULT 0,
  created_at DATETIME(3) NOT NULL,
  updated_at DATETIME(3) NOT NULL,
  closed_at DATETIME(3) NULL,
  UNIQUE KEY uk_customer_service_case_no (case_no),
  KEY idx_cs_case_status (status, priority, sla_due_at),
  KEY idx_cs_case_buyer (buyer_id, created_at),
  KEY idx_cs_case_merchant (merchant_id, created_at)
);

CREATE TABLE im_conversation (
  id BIGINT PRIMARY KEY,
  conversation_no VARCHAR(64) NOT NULL,
  buyer_id BIGINT NOT NULL,
  merchant_id BIGINT NOT NULL,
  shop_id BIGINT NOT NULL,
  context_type VARCHAR(32) NULL,
  context_id VARCHAR(128) NULL,
  status VARCHAR(32) NOT NULL,
  last_message_id BIGINT NULL,
  last_message_at DATETIME(3) NULL,
  created_at DATETIME(3) NOT NULL,
  updated_at DATETIME(3) NOT NULL,
  UNIQUE KEY uk_im_conversation_no (conversation_no),
  KEY idx_im_buyer (buyer_id, last_message_at),
  KEY idx_im_shop (shop_id, last_message_at)
);

CREATE TABLE im_message (
  id BIGINT PRIMARY KEY,
  message_no VARCHAR(64) NOT NULL,
  conversation_id BIGINT NOT NULL,
  sender_type VARCHAR(32) NOT NULL,
  sender_id BIGINT NOT NULL,
  client_message_id VARCHAR(128) NOT NULL,
  content_type VARCHAR(32) NOT NULL,
  content_text VARCHAR(4000) NULL,
  content_ref_json JSON NULL,
  attachment_file_ids_json JSON NULL,
  moderation_status VARCHAR(32) NOT NULL,
  delivery_status VARCHAR(32) NOT NULL,
  created_at DATETIME(3) NOT NULL,
  UNIQUE KEY uk_im_message_no (message_no),
  UNIQUE KEY uk_im_client_idem (conversation_id, sender_type, sender_id, client_message_id),
  KEY idx_im_message_conversation (conversation_id, id)
);

CREATE TABLE im_read_cursor (
  id BIGINT PRIMARY KEY,
  conversation_id BIGINT NOT NULL,
  participant_type VARCHAR(32) NOT NULL,
  participant_id BIGINT NOT NULL,
  last_read_message_id BIGINT NULL,
  updated_at DATETIME(3) NOT NULL,
  UNIQUE KEY uk_im_read_cursor (conversation_id, participant_type, participant_id)
);
