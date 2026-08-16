package com.example.evcharging.operation.asset;

import org.slf4j.*;
import org.springframework.stereotype.Service;

@Service
public class DeviceStationResolver {
    private static final Logger log=LoggerFactory.getLogger(DeviceStationResolver.class);
    private final AssetDeviceContextClient asset;

    public DeviceStationResolver(AssetDeviceContextClient asset){this.asset=asset;}

    public Long resolve(String deviceId){
        try{
            var ctx=asset.context(deviceId);
            return ctx.stationId()>0?ctx.stationId():null;
        }catch(Exception e){
            // Alarm ingestion must not be lost only because Asset is temporarily unavailable.
            // Null station remains fail-closed for station-scoped merchant queries.
            log.warn("cannot resolve station for alarm device={}",deviceId,e);
            return null;
        }
    }
}
