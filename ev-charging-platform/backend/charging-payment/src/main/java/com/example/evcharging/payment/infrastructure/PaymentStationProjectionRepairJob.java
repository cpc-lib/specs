package com.example.evcharging.payment.infrastructure;

import com.example.evcharging.framework.context.RequestContext;
import com.example.evcharging.payment.integration.CoreOrderClient;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PaymentStationProjectionRepairJob {
    private final JdbcTemplate jdbc;private final CoreOrderClient core;
    public PaymentStationProjectionRepairJob(JdbcTemplate jdbc,CoreOrderClient core){this.jdbc=jdbc;this.core=core;}

    @Scheduled(fixedDelayString="${charging.payment.station-projection-repair-ms:60000}")
    public void scan(){
        List<Row> rows=jdbc.query("""
            SELECT tenant_id,payment_no,biz_order_no FROM payment_order
            WHERE station_id IS NULL ORDER BY id LIMIT 100
            """,(rs,n)->new Row(rs.getLong(1),rs.getString(2),rs.getString(3)));
        for(Row r:rows){
            try{
                RequestContext.set(r.tenantId(),null,"payment-station-repair:"+r.paymentNo());
                var snapshot=core.paymentSnapshot(r.orderNo());
                jdbc.update("UPDATE payment_order SET station_id=?,update_time=NOW(3) WHERE tenant_id=? AND payment_no=? AND station_id IS NULL",
                        snapshot.stationId(),r.tenantId(),r.paymentNo());
                jdbc.update("UPDATE payment_refund SET station_id=?,update_time=NOW(3) WHERE tenant_id=? AND payment_no=? AND station_id IS NULL",
                        snapshot.stationId(),r.tenantId(),r.paymentNo());
            }catch(Exception ignored){
                // Retry later; historical projection repair is eventually consistent.
            }finally{RequestContext.clear();}
        }
    }
    private record Row(long tenantId,String paymentNo,String orderNo){}
}
