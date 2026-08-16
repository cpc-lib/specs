package com.example.evcharging.finance.ledger;

import com.example.evcharging.framework.event.DomainEventEnvelope;
import com.example.evcharging.framework.id.IdGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Component
public class PaymentLedgerConsumer {
    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper;
    private final IdGenerator ids;
    private final LedgerPostingService postingService;

    public PaymentLedgerConsumer(JdbcTemplate jdbc, ObjectMapper mapper, IdGenerator ids, LedgerPostingService postingService) {
        this.jdbc = jdbc;
        this.mapper = mapper;
        this.ids = ids;
        this.postingService = postingService;
    }

    @KafkaListener(topics="${charging.payment-event-topic:ev.payment.payment.v1}", groupId="charging-finance-ledger-v1")
    @Transactional
    public void consume(String raw) throws Exception {
        DomainEventEnvelope<JsonNode> event = mapper.readValue(raw, new TypeReference<DomainEventEnvelope<JsonNode>>() {});
        try {
            jdbc.update("INSERT INTO finance_event_inbox(id,consumer_name,event_id,event_type,processed_time) VALUES (?,?,?,?,?)",
                    ids.nextId(), "ledger-v1", event.eventId(), event.eventType(), LocalDateTime.now());
        } catch (DuplicateKeyException duplicate) {
            return;
        }

        long amount = event.payload().path("amountFen").asLong();
        if (amount <= 0) throw new IllegalArgumentException("invalid ledger amount");
        LedgerPosting posting;
        String bizNo;
        if ("payment.payment.succeeded".equals(event.eventType())) {
            bizNo = event.payload().path("paymentNo").asText();
            posting = new LedgerPosting(List.of(
                    new LedgerPosting.Entry("PAYMENT_CHANNEL_RECEIVABLE", LedgerPosting.Side.DEBIT, amount),
                    new LedgerPosting.Entry("CHARGING_RECEIVABLE_CLEARING", LedgerPosting.Side.CREDIT, amount)));
        } else if ("payment.refund.succeeded".equals(event.eventType())) {
            bizNo = event.payload().path("refundNo").asText();
            posting = new LedgerPosting(List.of(
                    new LedgerPosting.Entry("CHARGING_RECEIVABLE_CLEARING", LedgerPosting.Side.DEBIT, amount),
                    new LedgerPosting.Entry("PAYMENT_CHANNEL_RECEIVABLE", LedgerPosting.Side.CREDIT, amount)));
        } else {
            return;
        }
        postingService.post(event.tenantId(), event.eventId(), event.eventType(), bizNo,
                LocalDateTime.ofInstant(event.occurredAt(), ZoneOffset.UTC), posting);
    }
}
