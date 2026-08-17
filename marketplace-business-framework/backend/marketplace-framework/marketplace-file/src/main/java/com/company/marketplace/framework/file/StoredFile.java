package com.company.marketplace.framework.file; public record StoredFile(FileId id, String bucket, String objectKey, String contentType, long size) {}
