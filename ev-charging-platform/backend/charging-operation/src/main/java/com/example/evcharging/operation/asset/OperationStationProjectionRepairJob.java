package com.example.evcharging.operation.asset;

import com.example.evcharging.framework.context.RequestContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OperationStationProjectionRepairJob {
    private final JdbcTemplate jdbc;private final AssetDeviceContextClient asset;
    public OperationStationProjectionRepairJob(JdbcTemplate jdbc,AssetDeviceContextClient asset){this.jdbc=jdbc;this.asset=asset;}

    @Scheduled(fixedDelayString="${charging.operation.station-projection-repair-ms:120000}")
    public void scan(){
        List<Row> rows=jdbc.query("""
            SELECT id,tenant_id,device_id FROM operation_alarm
            WHERE station_id IS NULL ORDER BY id LIMIT 100
            """,(rs,n)->new Row(rs.getLong(1),rs.getLong(2),rs.getString(3)));
        for(Row r:rows){
            try{
                RequestContext.set(r.tenantId(),null,"operation-station-repair:"+r.alarmId());
                long station=asset.context(r.deviceId()).stationId();
                jdbc.update("UPDATE operation_alarm SET station_id=?,update_time=NOW(3) WHERE id=? AND station_id IS NULL",station,r.alarmId());
                jdbc.update("UPDATE operation_work_order SET station_id=?,update_time=NOW(3) WHERE alarm_id=? AND station_id IS NULL",station,r.alarmId());
            }catch(Exception ignored){
                // Keep null so STATION scope fails closed until authoritative Asset data is available.
            }finally{RequestContext.clear();}
        }
    }
    private record Row(long alarmId,long tenantId,String deviceId){}
}
