CREATE TABLE buyer_360_projection (
  buyer_id BIGINT PRIMARY KEY,
  profile_json JSON NULL,
  member_json JSON NULL,
  order_summary_json JSON NULL,
  favorite_follow_json JSON NULL,
  coupon_summary_json JSON NULL,
  review_summary_json JSON NULL,
  aftersale_summary_json JSON NULL,
  service_case_summary_json JSON NULL,
  permitted_risk_summary_json JSON NULL,
  source_version BIGINT NOT NULL DEFAULT 0,
  updated_at DATETIME(3) NOT NULL
);
