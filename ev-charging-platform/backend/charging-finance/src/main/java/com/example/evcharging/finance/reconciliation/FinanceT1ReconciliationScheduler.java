package com.example.evcharging.finance.reconciliation;

import com.example.evcharging.framework.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.*;
import java.util.List;

@Component
public class FinanceT1ReconciliationScheduler {
    private static final Logger log=LoggerFactory.getLogger(FinanceT1ReconciliationScheduler.class);
    private final JdbcTemplate jdbc;
    private final ReconciliationApplicationService reconciliation;

    public FinanceT1ReconciliationScheduler(JdbcTemplate jdbc,ReconciliationApplicationService reconciliation){
        this.jdbc=jdbc;this.reconciliation=reconciliation;
    }

    private record ScheduleRow(long id,long tenant,String channel,String merchant,String zoneId,LocalDate lastSuccess){}

    @Scheduled(cron="${charging.finance.reconciliation-scan-cron:0 15 2 * * *}")
    public void scan(){
        List<ScheduleRow> rows=jdbc.query("""
            SELECT id,tenant_id,channel,merchant_id,zone_id,last_success_business_date
            FROM finance_reconciliation_schedule WHERE enabled=b'1' ORDER BY id
            """,(rs,n)->new ScheduleRow(rs.getLong(1),rs.getLong(2),rs.getString(3),rs.getString(4),rs.getString(5),
                rs.getObject(6)==null?null:rs.getDate(6).toLocalDate()));
        for(ScheduleRow row:rows) runOne(row);
    }

    private void runOne(ScheduleRow row){
        ZoneId zone=ZoneId.of(row.zoneId());
        LocalDate businessDate=LocalDate.now(zone).minusDays(1);
        if(row.lastSuccess()!=null&&!row.lastSuccess().isBefore(businessDate)) return;
        Integer billCount=jdbc.queryForObject("""
            SELECT COUNT(*) FROM finance_channel_bill_batch
            WHERE tenant_id=? AND channel=? AND merchant_id=? AND business_date=? AND status='IMPORTED'
            """,Integer.class,row.tenant(),row.channel(),row.merchant(),businessDate);
        if(billCount==null||billCount==0){log.info("T+1 reconciliation waiting for channel bill tenant={} channel={} merchant={} date={}",row.tenant(),row.channel(),row.merchant(),businessDate);return;}
        String requestId="T1:"+row.channel()+":"+row.merchant()+":"+businessDate;
        RequestContext.set(row.tenant(),0L,requestId);
        try{
            reconciliation.run(new ReconciliationApplicationService.RunRequest(requestId,row.channel(),row.merchant(),businessDate));
            jdbc.update("UPDATE finance_reconciliation_schedule SET last_success_business_date=?,update_time=NOW(3) WHERE id=?",businessDate,row.id());
        }catch(Exception e){log.error("T+1 reconciliation failed tenant={} channel={} merchant={} date={}",row.tenant(),row.channel(),row.merchant(),businessDate,e);}
        finally{RequestContext.clear();}
    }
}
