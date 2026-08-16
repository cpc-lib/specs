package com.example.evcharging.asset.charger;

import com.example.evcharging.framework.context.RequestContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/internal-api/v1/assets/devices")
public class InternalDeviceContextController {
    private final JdbcTemplate jdbc;
    public InternalDeviceContextController(JdbcTemplate jdbc){this.jdbc=jdbc;}

    @GetMapping("/{deviceId}/context")
    public DeviceContext context(@PathVariable String deviceId){
        long tenant=RequestContext.requireTenantId();
        List<DeviceContext> rows=jdbc.query("""
            SELECT id,station_id,charger_code,device_sn
            FROM charger
            WHERE tenant_id=? AND deleted=0 AND (device_sn=? OR charger_code=?)
            ORDER BY id LIMIT 1
            """,(rs,n)->new DeviceContext(rs.getLong(1),rs.getLong(2),rs.getString(3),rs.getString(4)),
                tenant,deviceId,deviceId);
        if(rows.isEmpty()) throw new IllegalArgumentException("device asset context not found");
        return rows.get(0);
    }

    public record DeviceContext(long chargerId,long stationId,String chargerCode,String deviceSn){}
}
