package com.example.evcharging.open.regulatory;

import com.example.evcharging.framework.context.RequestContext;
import com.example.evcharging.open.integration.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class RegulatorySnapshotScheduler {
    private final JdbcTemplate jdbc;private final AssetOpenClient assets;private final CoreRegulatoryClient core;
    private final RegulatoryReportService reports;

    public RegulatorySnapshotScheduler(JdbcTemplate jdbc,AssetOpenClient assets,CoreRegulatoryClient core,RegulatoryReportService reports){
        this.jdbc=jdbc;this.assets=assets;this.core=core;this.reports=reports;
    }

    @Scheduled(fixedDelayString="${charging.open.regulatory-snapshot-ms:60000}")
    public void scan(){
        List<Platform> platforms=jdbc.query("""
            SELECT id,tenant_id,public_info_enabled,business_info_enabled
            FROM open_regulatory_platform WHERE enabled=1 ORDER BY id
            """,(rs,n)->new Platform(rs.getLong(1),rs.getLong(2),rs.getBoolean(3),rs.getBoolean(4)));
        for(Platform platform:platforms) snapshot(platform);
    }

    private void snapshot(Platform p){
        try{
            RequestContext.set(p.tenantId(),null,"regulatory-snapshot:"+p.id());
            if(p.publicInfo()){
                for(var station:assets.stations(new AssetOpenClient.StationQuery(true,Set.of())))
                    reports.enqueue(p.tenantId(),p.id(),"PUBLIC_STATION",String.valueOf(station.stationId()),station);
            }
            if(p.businessInfo()){
                for(var order:core.latest(500))
                    reports.enqueue(p.tenantId(),p.id(),"BUSINESS_ORDER",order.orderNo(),order);
            }
        }catch(Exception ignored){
            // One platform snapshot cannot block other regulatory platforms.
        }finally{RequestContext.clear();}
    }

    private record Platform(long id,long tenantId,boolean publicInfo,boolean businessInfo){}
}
