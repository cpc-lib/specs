package com.example.evcharging.finance.reconciliation;

public interface BillArchiveStorage {
    ArchiveResult archive(long tenantId, String originalFileName, byte[] bytes);
    record ArchiveResult(String objectKey, String sha256, long sizeBytes) {}
}
