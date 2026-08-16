package com.example.evcharging.asset.charger;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.evcharging.framework.id.IdGenerator;
import com.example.evcharging.asset.station.StationMapper;
import com.example.evcharging.asset.station.StationEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AssetDeviceApplicationService {
    private final ChargerMapper chargers; private final ConnectorMapper connectors; private final StationMapper stations; private final IdGenerator ids;
    public AssetDeviceApplicationService(ChargerMapper chargers, ConnectorMapper connectors, StationMapper stations, IdGenerator ids){this.chargers=chargers;this.connectors=connectors;this.stations=stations;this.ids=ids;}

    @Transactional public ChargerEntity createCharger(long tenantId,long stationId,CreateChargerRequest r){
        StationEntity station=stations.selectById(stationId);
        if(station==null || !tenantIdEquals(station.getTenantId(),tenantId)) throw new IllegalArgumentException("station not found");
        ChargerEntity e=new ChargerEntity(); e.setId(ids.nextId()); e.setTenantId(tenantId); e.setStationId(stationId);
        e.setChargerCode(r.chargerCode().trim()); e.setDeviceSn(r.deviceSn().trim()); e.setProtocolType(r.protocolType()==null?"SIM_V1":r.protocolType().trim());
        e.setOnlineStatus(0); e.setRunningStatus(0); e.setVersion(0); e.setDeleted(false); e.setCreateTime(LocalDateTime.now()); e.setUpdateTime(e.getCreateTime()); chargers.insert(e); return e;
    }
    @Transactional(readOnly=true) public List<ChargerEntity> listChargers(long tenantId,long stationId){return chargers.selectList(new LambdaQueryWrapper<ChargerEntity>().eq(ChargerEntity::getTenantId,tenantId).eq(ChargerEntity::getStationId,stationId).orderByDesc(ChargerEntity::getCreateTime));}
    @Transactional public ConnectorEntity createConnector(long tenantId,long chargerId,CreateConnectorRequest r){
        ChargerEntity charger=chargers.selectOne(new LambdaQueryWrapper<ChargerEntity>().eq(ChargerEntity::getTenantId,tenantId).eq(ChargerEntity::getId,chargerId));
        if(charger==null) throw new IllegalArgumentException("charger not found");
        ConnectorEntity e=new ConnectorEntity(); e.setId(ids.nextId()); e.setTenantId(tenantId); e.setStationId(charger.getStationId()); e.setChargerId(chargerId);
        e.setConnectorCode(r.connectorCode().trim()); e.setConnectorNo(r.connectorNo()); e.setConnectorType(r.connectorType()); e.setRatedPowerW(r.ratedPowerW()==null?60000L:r.ratedPowerW());
        e.setOnlineStatus(0); e.setRunningStatus(0); e.setVersion(0); e.setDeleted(false); e.setCreateTime(LocalDateTime.now()); e.setUpdateTime(e.getCreateTime()); connectors.insert(e); return e;
    }
    @Transactional(readOnly=true) public List<ConnectorEntity> listConnectors(long tenantId,long chargerId){return connectors.selectList(new LambdaQueryWrapper<ConnectorEntity>().eq(ConnectorEntity::getTenantId,tenantId).eq(ConnectorEntity::getChargerId,chargerId).orderByAsc(ConnectorEntity::getConnectorNo));}
    @Transactional(readOnly=true) public ConnectorSnapshot snapshot(long tenantId,String connectorCode){
        ConnectorEntity c=connectors.selectOne(new LambdaQueryWrapper<ConnectorEntity>().eq(ConnectorEntity::getTenantId,tenantId).eq(ConnectorEntity::getConnectorCode,connectorCode));
        if(c==null) throw new IllegalArgumentException("connector not found");
        ChargerEntity h=chargers.selectById(c.getChargerId()); if(h==null || !tenantIdEquals(h.getTenantId(),tenantId)) throw new IllegalArgumentException("charger not found");
        return new ConnectorSnapshot(c.getId(),c.getStationId(),c.getChargerId(),c.getConnectorCode(),c.getConnectorNo(),h.getDeviceSn(),c.getOnlineStatus(),c.getRunningStatus(),c.getRatedPowerW()==null?0:c.getRatedPowerW());
    }
    private boolean tenantIdEquals(Long value,long expected){return value!=null && value==expected;}
}
