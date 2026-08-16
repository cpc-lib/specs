package com.example.evcharging.finance.reconciliation;

import com.example.evcharging.finance.integration.CoreOrderProjectionClient;
import com.example.evcharging.framework.context.RequestContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FinanceStationProjectionRepairJob {
    private final JdbcTemplate jdbc;private final CoreOrderProjectionClient core;
    public FinanceStationProjectionRepairJob(JdbcTemplate jdbc,CoreOrderProjectionClient core){this.jdbc=jdbc;this.core=core;}

    @Scheduled(fixedDelayString="${charging.finance.station-projection-repair-ms:90000}")
    public void scan(){
        List<Row> rows=jdbc.query("""
            SELECT tenant_id,payment_no,biz_order_no FROM finance_transaction_fact
            WHERE station_id IS NULL ORDER BY id LIMIT 100
            """,(rs,n)->new Row(rs.getLong(1),rs.getString(2),rs.getString(3)));
        for(Row r:rows){
            try{
                RequestContext.set(r.tenantId(),null,"finance-station-repair:"+r.paymentNo());
                long station=core.snapshot(r.orderNo()).stationId();
                jdbc.update("UPDATE finance_transaction_fact SET station_id=?,update_time=NOW(3) WHERE tenant_id=? AND payment_no=? AND station_id IS NULL",
                        station,r.tenantId(),r.paymentNo());
                jdbc.update("UPDATE finance_refund_fact SET station_id=? WHERE tenant_id=? AND payment_no=? AND station_id IS NULL",
                        station,r.tenantId(),r.paymentNo());
                jdbc.update("UPDATE finance_settlement_source SET station_id=?,update_time=NOW(3) WHERE tenant_id=? AND payment_no=? AND station_id IS NULL",
                        station,r.tenantId(),r.paymentNo());
            }catch(Exception ignored){
                // Retry next scan; never invent station data.
            }finally{RequestContext.clear();}
        }
    }
    private record Row(long tenantId,String paymentNo,String orderNo){}
}
