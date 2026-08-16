package com.example.evcharging.finance.reconciliation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.*;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.util.HexFormat;

@Component
public class LocalBillArchiveStorage implements BillArchiveStorage {
    private final Path baseDir;
    public LocalBillArchiveStorage(@Value("${charging.finance.archive-dir:./build/finance-channel-bills}") String baseDir) {
        this.baseDir = Path.of(baseDir).toAbsolutePath().normalize();
    }

    @Override
    public ArchiveResult archive(long tenantId, String originalFileName, byte[] bytes) {
        try {
            String sha = sha256(bytes);
            String safe = originalFileName == null ? "channel-bill.json" : originalFileName.replaceAll("[^a-zA-Z0-9._-]", "_");
            LocalDate date = LocalDate.now();
            Path relative = Path.of(String.valueOf(tenantId), String.valueOf(date.getYear()), "%02d".formatted(date.getMonthValue()), sha + "-" + safe);
            Path target = baseDir.resolve(relative).normalize();
            if (!target.startsWith(baseDir)) throw new IllegalArgumentException("invalid archive path");
            Files.createDirectories(target.getParent());
            if (!Files.exists(target)) Files.write(target, bytes, StandardOpenOption.CREATE_NEW);
            return new ArchiveResult(relative.toString().replace('\\','/'), sha, bytes.length);
        } catch (Exception e) {
            throw new IllegalStateException("cannot archive channel bill", e);
        }
    }

    private static String sha256(byte[] bytes) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
    }
}
