package com.example.evcharging.finance.ledger;

import com.example.evcharging.framework.id.IdGenerator;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class LedgerPostingService {
    private final JdbcTemplate jdbc;
    private final IdGenerator ids;

    public LedgerPostingService(JdbcTemplate jdbc, IdGenerator ids) {
        this.jdbc = jdbc;
        this.ids = ids;
    }

    public boolean post(long tenantId, String eventId, String bizType, String bizNo,
                        LocalDateTime occurredAt, LedgerPosting posting) {
        long txId = ids.nextId();
        long debit = posting.entries().stream().filter(x -> x.side() == LedgerPosting.Side.DEBIT)
                .mapToLong(LedgerPosting.Entry::amountFen).sum();
        long credit = posting.entries().stream().filter(x -> x.side() == LedgerPosting.Side.CREDIT)
                .mapToLong(LedgerPosting.Entry::amountFen).sum();
        LocalDateTime now = LocalDateTime.now();
        try {
            jdbc.update("""
                INSERT INTO finance_ledger_transaction(
                  id,tenant_id,transaction_no,biz_event_id,biz_type,biz_no,currency,
                  total_debit_fen,total_credit_fen,occurred_time,create_time
                ) VALUES (?,?,?,?,?,?,?,?,?,?,?)
                """, txId, tenantId, "LT" + txId, eventId, bizType, bizNo, "CNY",
                    debit, credit, occurredAt, now);
        } catch (DuplicateKeyException duplicate) {
            return false;
        }

        for (LedgerPosting.Entry entry : posting.entries()) {
            ensureAccount(tenantId, entry.accountCode(), entry.side(), now);
            jdbc.update("""
                INSERT INTO finance_ledger_entry(
                  id,tenant_id,transaction_id,account_code,subject_type,subject_id,
                  entry_side,amount_fen,currency,memo,create_time
                ) VALUES (?,?,?,?,?,?,?,?,?,?,?)
                """, ids.nextId(), tenantId, txId, entry.accountCode(), entry.subjectType(), entry.subjectId(),
                    entry.side().name(), entry.amountFen(), "CNY", bizType + ":" + bizNo, now);
        }
        return true;
    }

    private void ensureAccount(long tenantId, String accountCode, LedgerPosting.Side side, LocalDateTime now) {
        String normal = side == LedgerPosting.Side.DEBIT ? "DEBIT" : "CREDIT";
        jdbc.update("""
            INSERT IGNORE INTO finance_ledger_account(
              id,tenant_id,account_code,account_name,normal_side,status,create_time
            ) VALUES (?,?,?,?,?,'ACTIVE',?)
            """, ids.nextId(), tenantId, accountCode, humanName(accountCode), normal, now);
    }

    private static String humanName(String code) {
        return switch (code) {
            case "PAYMENT_CHANNEL_RECEIVABLE" -> "渠道应收";
            case "CHARGING_RECEIVABLE_CLEARING" -> "充电应收清算";
            case "PLATFORM_REVENUE" -> "平台收入";
            default -> code;
        };
    }
}
