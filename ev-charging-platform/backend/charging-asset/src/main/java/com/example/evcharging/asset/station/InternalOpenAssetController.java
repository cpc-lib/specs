package com.example.evcharging.asset.station;

import com.example.evcharging.framework.context.RequestContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/internal-api/v1/assets/open")
public class InternalOpenAssetController {
    private final JdbcTemplate jdbc;
    public InternalOpenAssetController(JdbcTemplate jdbc){this.jdbc=jdbc;}

    @PostMapping("/stations/query")
    public List<StationView> stations(@RequestBody StationQuery q){
        long tenant=RequestContext.requireTenantId();
        StringBuilder sql=new StringBuilder("""
            SELECT s.id,s.station_code,s.station_name,s.longitude,s.latitude,
                   COUNT(c.id),SUM(CASE WHEN c.running_status=0 AND c.online_status=1 THEN 1 ELSE 0 END)
            FROM station s LEFT JOIN charger_connector c ON c.station_id=s.id AND c.deleted=0
            WHERE s.tenant_id=? AND s.deleted=0 AND s.status=1
            """);
        List<Object> args=new ArrayList<>();args.add(tenant);
        if(!q.allStations()){
            if(q.stationIds()==null||q.stationIds().isEmpty())return List.of();
            sql.append(" AND s.id IN (").append(String.join(",",Collections.nCopies(q.stationIds().size(),"?"))).append(")");
            args.addAll(q.stationIds());
        }
        sql.append(" GROUP BY s.id,s.station_code,s.station_name,s.longitude,s.latitude ORDER BY s.id");
        return jdbc.query(sql.toString(),(rs,n)->new StationView(rs.getLong(1),rs.getString(2),rs.getString(3),
                decimal(rs.getBigDecimal(4)),decimal(rs.getBigDecimal(5)),rs.getInt(6),rs.getInt(7)),args.toArray());
    }

    @GetMapping("/connectors/{connectorCode}/context")
    public ConnectorContext connector(@PathVariable String connectorCode){
        long tenant=RequestContext.requireTenantId();
        return jdbc.queryForObject("""
            SELECT c.id,c.station_id,c.charger_id,c.connector_code,c.online_status,c.running_status
            FROM charger_connector c
            WHERE c.tenant_id=? AND c.connector_code=? AND c.deleted=0
            """,(rs,n)->new ConnectorContext(rs.getLong(1),rs.getLong(2),rs.getLong(3),rs.getString(4),
                rs.getInt(5),rs.getInt(6)),tenant,connectorCode);
    }

    @GetMapping("/stations/{stationId}")
    public StationDetail detail(@PathVariable long stationId){
        long tenant=RequestContext.requireTenantId();
        StationView station=jdbc.queryForObject("""
            SELECT s.id,s.station_code,s.station_name,s.longitude,s.latitude,
                   COUNT(c.id),SUM(CASE WHEN c.running_status=0 AND c.online_status=1 THEN 1 ELSE 0 END)
            FROM station s LEFT JOIN charger_connector c ON c.station_id=s.id AND c.deleted=0
            WHERE s.tenant_id=? AND s.id=? AND s.deleted=0 AND s.status=1
            GROUP BY s.id,s.station_code,s.station_name,s.longitude,s.latitude
            """,(rs,n)->new StationView(rs.getLong(1),rs.getString(2),rs.getString(3),decimal(rs.getBigDecimal(4)),
                decimal(rs.getBigDecimal(5)),rs.getInt(6),rs.getInt(7)),tenant,stationId);
        List<ConnectorView> connectors=jdbc.query("""
            SELECT c.id,c.connector_code,c.connector_no,c.connector_type,c.rated_power_w,c.online_status,c.running_status,ch.charger_code
            FROM charger_connector c JOIN charger ch ON ch.id=c.charger_id
            WHERE c.tenant_id=? AND c.station_id=? AND c.deleted=0 ORDER BY ch.charger_code,c.connector_no
            """,(rs,n)->new ConnectorView(rs.getLong(1),rs.getString(2),rs.getInt(3),rs.getInt(4),
                (Long)rs.getObject(5),rs.getInt(6),rs.getInt(7),rs.getString(8)),tenant,stationId);
        return new StationDetail(station,connectors);
    }

    private static Double decimal(BigDecimal value){return value==null?null:value.doubleValue();}
    public record ConnectorContext(long connectorId,long stationId,long chargerId,String connectorCode,int onlineStatus,int runningStatus){}
    public record StationQuery(boolean allStations,Set<Long> stationIds){}
    public record StationView(long stationId,String stationCode,String stationName,Double longitude,Double latitude,
                              int connectorCount,int availableConnectors){}
    public record ConnectorView(long connectorId,String connectorCode,int connectorNo,int connectorType,Long ratedPowerW,
                                int onlineStatus,int runningStatus,String chargerCode){}
    public record StationDetail(StationView station,List<ConnectorView> connectors){}
}
