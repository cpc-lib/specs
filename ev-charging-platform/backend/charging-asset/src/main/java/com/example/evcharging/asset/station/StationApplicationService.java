package com.example.evcharging.asset.station;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.evcharging.framework.context.RequestContext;
import com.example.evcharging.framework.id.IdGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.UUID;

@Service
public class StationApplicationService {
    private final StationMapper stationMapper;
    private final JdbcTemplate jdbcTemplate;
    private final IdGenerator idGenerator;
    private final ObjectMapper objectMapper;

    public StationApplicationService(StationMapper stationMapper, JdbcTemplate jdbcTemplate,
                                     IdGenerator idGenerator, ObjectMapper objectMapper) {
        this.stationMapper = stationMapper;
        this.jdbcTemplate = jdbcTemplate;
        this.idGenerator = idGenerator;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public StationEntity create(long tenantId, CreateStationRequest request) {
        StationEntity entity = new StationEntity();
        entity.setId(idGenerator.nextId());
        entity.setTenantId(tenantId);
        entity.setOperatorId(request.operatorId());
        entity.setStationCode(request.stationCode().trim());
        entity.setStationName(request.stationName().trim());
        entity.setStatus(1);
        entity.setVersion(0);
        entity.setDeleted(false);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(entity.getCreateTime());
        stationMapper.insert(entity);

        String payload = toJson(Map.of(
                "stationId", entity.getId(),
                "stationCode", entity.getStationCode(),
                "stationName", entity.getStationName()
        ));

        jdbcTemplate.update(
                "INSERT INTO event_outbox(id,tenant_id,event_id,aggregate_type,aggregate_id,event_type,event_version,payload,trace_id,status,retry_count,occurred_time,create_time) "
                        + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)",
                idGenerator.nextId(), tenantId, UUID.randomUUID().toString(),
                "Station", String.valueOf(entity.getId()),
                "asset.station.created", "1.0", payload,
                RequestContext.requestId(), 0, 0, LocalDateTime.now(), LocalDateTime.now());
        return entity;
    }

    @Transactional(readOnly = true)
    public List<StationEntity> list(long tenantId) {
        return stationMapper.selectList(new LambdaQueryWrapper<StationEntity>()
                .eq(StationEntity::getTenantId, tenantId)
                .orderByDesc(StationEntity::getCreateTime));
    }

    @Transactional(readOnly = true)
    public List<StationEntity> listScoped(long tenantId, Set<Long> stationIds, boolean allTenantStations) {
        LambdaQueryWrapper<StationEntity> q=new LambdaQueryWrapper<StationEntity>()
                .eq(StationEntity::getTenantId,tenantId)
                .eq(StationEntity::getDeleted,false)
                .orderByDesc(StationEntity::getCreateTime);
        if(!allTenantStations){
            if(stationIds==null||stationIds.isEmpty()) return List.of();
            q.in(StationEntity::getId,stationIds);
        }
        return stationMapper.selectList(q);
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("cannot serialize station event payload", e);
        }
    }
    @Transactional(readOnly=true) public boolean exists(long tenantId,long stationId){StationEntity station=stationMapper.selectById(stationId);return station!=null && station.getTenantId()!=null && station.getTenantId()==tenantId && !Boolean.TRUE.equals(station.getDeleted());}
}
