package com.example.evcharging.finance.reconciliation;

import com.example.evcharging.framework.context.RequestContext;
import com.example.evcharging.framework.id.IdGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ChannelBillRawImportService {
    private final BillArchiveStorage storage;
    private final ChannelBillApplicationService importService;
    private final ObjectMapper mapper;
    private final JdbcTemplate jdbc;
    private final IdGenerator ids;

    public ChannelBillRawImportService(BillArchiveStorage storage, ChannelBillApplicationService importService,
                                       ObjectMapper mapper, JdbcTemplate jdbc, IdGenerator ids) {
        this.storage = storage;
        this.importService = importService;
        this.mapper = mapper;
        this.jdbc = jdbc;
        this.ids = ids;
    }

    public String importJson(MultipartFile file) {
        long tenant = RequestContext.requireTenantId();
        try {
            byte[] bytes = file.getBytes();
            BillArchiveStorage.ArchiveResult archive = storage.archive(tenant, file.getOriginalFilename(), bytes);
            JsonNode root = mapper.readTree(bytes);
            String channel = required(root, "channel");
            String merchant = root.path("merchantId").asText("DEFAULT");
            LocalDate businessDate = LocalDate.parse(required(root, "businessDate"));
            List<ChannelBillApplicationService.ChannelTransactionRequest> records = new ArrayList<>();
            for (JsonNode row : root.path("records")) {
                records.add(new ChannelBillApplicationService.ChannelTransactionRequest(
                        blank(row.path("paymentNo").asText()), required(row,"channelTradeNo"),
                        row.path("amountFen").asLong(), row.path("refundAmountFen").asLong(0),
                        required(row,"channelStatus"), LocalDateTime.parse(required(row,"occurredTime"))));
            }
            String batchNo = importService.importBill(new ChannelBillApplicationService.ImportRequest(
                    channel, merchant, businessDate, file.getOriginalFilename(), archive.sha256(), records));
            Long batchId = jdbc.queryForObject("SELECT id FROM finance_channel_bill_batch WHERE tenant_id=? AND batch_no=?",
                    Long.class, tenant, batchNo);
            jdbc.update("""
                INSERT IGNORE INTO finance_channel_bill_archive(
                  id,tenant_id,batch_id,object_key,sha256,size_bytes,media_type,original_file_name,create_time
                ) VALUES (?,?,?,?,?,?,?,?,?)
                """, ids.nextId(),tenant,batchId,archive.objectKey(),archive.sha256(),archive.sizeBytes(),
                    file.getContentType(),file.getOriginalFilename(),LocalDateTime.now());
            return batchNo;
        } catch (Exception e) {
            if (e instanceof RuntimeException runtime) throw runtime;
            throw new IllegalStateException("raw channel bill import failed", e);
        }
    }

    private static String required(JsonNode node,String name){String v=node.path(name).asText();if(v==null||v.isBlank())throw new IllegalArgumentException(name+" required");return v;}
    private static String blank(String v){return v==null||v.isBlank()?null:v;}
}
