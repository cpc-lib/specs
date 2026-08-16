package com.example.evcharging.open.callback;

import com.example.evcharging.framework.context.RequestContext;
import com.example.evcharging.open.integration.CorePartnerClient;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class PartnerBusinessPoller {
    private final JdbcTemplate jdbc;private final CorePartnerClient core;private final PartnerCallbackService callbacks;

    public PartnerBusinessPoller(JdbcTemplate jdbc,CorePartnerClient core,PartnerCallbackService callbacks){
        this.jdbc=jdbc;this.core=core;this.callbacks=callbacks;
    }

    @Scheduled(fixedDelayString="${charging.open.partner-business-scan-ms:10000}")
    public void scan(){
        List<Row> rows=jdbc.query("""
            SELECT r.tenant_id,r.partner_id,r.external_user_id,r.session_no,m.local_user_id
            FROM open_partner_charging_ref r
            JOIN open_partner_user_mapping m ON m.partner_id=r.partner_id AND m.external_user_id=r.external_user_id
            JOIN open_partner_app p ON p.id=r.partner_id
            WHERE p.status='ACTIVE' AND p.callback_url IS NOT NULL
            ORDER BY r.id DESC LIMIT 200
            """,(rs,n)->new Row(rs.getLong(1),rs.getLong(2),rs.getString(3),rs.getString(4),rs.getLong(5)));
        for(Row row:rows)poll(row);
    }

    private void poll(Row row){
        try{
            RequestContext.set(row.tenantId(),null,"partner-callback-poll:"+row.sessionNo());
            var session=core.session(row.sessionNo(),row.localUserId());
            callbacks.schedule(row.tenantId(),row.partnerId(),"CHARGING_STATUS",
                    row.sessionNo()+":"+session.status(),
                    Map.of("event","CHARGING_STATUS","sessionNo",row.sessionNo(),"status",session.status(),
                           "energyWh",session.energyWh(),"soc",session.soc()==null?-1:session.soc(),
                           "powerW",session.powerW()==null?0:session.powerW()));
            if(session.orderNo()!=null&&!session.orderNo().isBlank()){
                var order=core.order(session.orderNo());
                callbacks.schedule(row.tenantId(),row.partnerId(),"ORDER_CREATED",order.orderNo(),
                        Map.of("event","ORDER_CREATED","orderNo",order.orderNo(),"sessionNo",order.sessionNo(),
                               "stationId",order.stationId(),"energyWh",order.energyWh(),
                               "receivableAmountFen",order.receivableAmountFen(),"paidAmountFen",order.paidAmountFen(),
                               "tradeStatus",order.tradeStatus(),"paymentStatus",order.paymentStatus()));
            }
        }catch(Exception ignored){
            // Polling is eventually consistent; the next run retries.
        }finally{RequestContext.clear();}
    }

    private record Row(long tenantId,long partnerId,String externalUserId,String sessionNo,long localUserId){}
}
