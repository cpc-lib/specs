# File boundary
Business tables store `fileId`/evidence reference, not long-lived MinIO URLs.
Authorization and signed download URLs are resolved at access time.
Product media, qualification evidence, review media and aftersale/dispute evidence share this abstraction but retain domain ownership of metadata.
