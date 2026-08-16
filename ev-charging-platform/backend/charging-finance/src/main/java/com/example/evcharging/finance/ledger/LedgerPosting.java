package com.example.evcharging.finance.ledger;

import java.util.List;

public record LedgerPosting(List<Entry> entries) {
    public LedgerPosting {
        if (entries == null || entries.isEmpty()) throw new IllegalArgumentException("ledger entries required");
        long debit = entries.stream().filter(e -> e.side() == Side.DEBIT).mapToLong(Entry::amountFen).sum();
        long credit = entries.stream().filter(e -> e.side() == Side.CREDIT).mapToLong(Entry::amountFen).sum();
        if (debit <= 0 || debit != credit) {
            throw new IllegalArgumentException("ledger transaction must balance: debit=" + debit + ", credit=" + credit);
        }
    }

    public enum Side { DEBIT, CREDIT }

    public record Entry(String accountCode, String subjectType, String subjectId, Side side, long amountFen) {
        public Entry(String accountCode, Side side, long amountFen) {
            this(accountCode, null, null, side, amountFen);
        }
        public Entry {
            if (accountCode == null || accountCode.isBlank()) throw new IllegalArgumentException("accountCode required");
            if (side == null) throw new IllegalArgumentException("side required");
            if (amountFen <= 0) throw new IllegalArgumentException("amount must be positive");
        }
    }
}
