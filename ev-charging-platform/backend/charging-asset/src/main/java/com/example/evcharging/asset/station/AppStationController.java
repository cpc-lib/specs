package com.example.evcharging.asset.station;

import com.example.evcharging.framework.api.ApiResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/app-api/v1/stations")
public class AppStationController {
    private final JdbcTemplate jdbc;
    public AppStationController(JdbcTemplate jdbc){this.jdbc=jdbc;}

    @GetMapping
    public ApiResponse<List<StationCard>> nearby(@RequestParam(defaultValue="1") long tenantId,
                                                 @RequestParam(required=false) Double longitude,
                                                 @RequestParam(required=false) Double latitude,
                                                 @RequestParam(defaultValue="50") int limit){
        int size=Math.max(1,Math.min(limit,100));
        return ApiResponse.success(jdbc.query("""
            SELECT s.id,s.station_code,s.station_name,s.longitude,s.latitude,
                   COUNT(c.id) connector_count,
                   SUM(CASE WHEN c.running_status=0 AND c.online_status=1 THEN 1 ELSE 0 END) available_connectors
            FROM station s
            LEFT JOIN charger_connector c ON c.station_id=s.id AND c.deleted=0
            WHERE s.tenant_id=? AND s.deleted=0 AND s.status=1
            GROUP BY s.id,s.station_code,s.station_name,s.longitude,s.latitude
            ORDER BY available_connectors DESC,s.id DESC LIMIT ?
            """,(rs,n)->new StationCard(rs.getLong(1),rs.getString(2),rs.getString(3),
                decimal(rs.getBigDecimal(4)),decimal(rs.getBigDecimal(5)),rs.getInt(6),rs.getInt(7)),tenantId,size));
    }

    @GetMapping("/{stationId}")
    public ApiResponse<StationDetail> detail(@PathVariable long stationId,@RequestParam(defaultValue="1") long tenantId){
        StationCard station=jdbc.queryForObject("""
            SELECT s.id,s.station_code,s.station_name,s.longitude,s.latitude,
                   COUNT(c.id),SUM(CASE WHEN c.running_status=0 AND c.online_status=1 THEN 1 ELSE 0 END)
            FROM station s LEFT JOIN charger_connector c ON c.station_id=s.id AND c.deleted=0
            WHERE s.tenant_id=? AND s.id=? AND s.deleted=0
            GROUP BY s.id,s.station_code,s.station_name,s.longitude,s.latitude
            """,(rs,n)->new StationCard(rs.getLong(1),rs.getString(2),rs.getString(3),decimal(rs.getBigDecimal(4)),
                decimal(rs.getBigDecimal(5)),rs.getInt(6),rs.getInt(7)),tenantId,stationId);
        List<ConnectorCard> connectors=jdbc.query("""
            SELECT c.id,c.connector_code,c.connector_no,c.connector_type,c.rated_power_w,
                   c.online_status,c.running_status,ch.charger_code
            FROM charger_connector c JOIN charger ch ON ch.id=c.charger_id
            WHERE c.tenant_id=? AND c.station_id=? AND c.deleted=0
            ORDER BY ch.charger_code,c.connector_no
            """,(rs,n)->new ConnectorCard(rs.getLong(1),rs.getString(2),rs.getInt(3),rs.getInt(4),
                (Long)rs.getObject(5),rs.getInt(6),rs.getInt(7),rs.getString(8)),tenantId,stationId);
        return ApiResponse.success(new StationDetail(station,connectors));
    }

    public record StationCard(long stationId,String stationCode,String stationName,Double longitude,Double latitude,
                              int connectorCount,int availableConnectors){}
    public record ConnectorCard(long connectorId,String connectorCode,int connectorNo,int connectorType,Long ratedPowerW,
                                int onlineStatus,int runningStatus,String chargerCode){}
    public record StationDetail(StationCard station,List<ConnectorCard> connectors){}
    private static Double decimal(java.math.BigDecimal value){return value==null?null:value.doubleValue();}
}
