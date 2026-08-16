package com.example.evcharging.asset.charger;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

@TableName("charger")
public class ChargerEntity {
    @TableId(type = IdType.INPUT) private Long id;
    private Long tenantId;
    private Long stationId;
    private String chargerCode;
    private String deviceSn;
    private String protocolType;
    private Integer onlineStatus;
    private Integer runningStatus;
    @Version private Integer version;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic private Boolean deleted;
    public Long getId(){return id;} public void setId(Long v){id=v;}
    public Long getTenantId(){return tenantId;} public void setTenantId(Long v){tenantId=v;}
    public Long getStationId(){return stationId;} public void setStationId(Long v){stationId=v;}
    public String getChargerCode(){return chargerCode;} public void setChargerCode(String v){chargerCode=v;}
    public String getDeviceSn(){return deviceSn;} public void setDeviceSn(String v){deviceSn=v;}
    public String getProtocolType(){return protocolType;} public void setProtocolType(String v){protocolType=v;}
    public Integer getOnlineStatus(){return onlineStatus;} public void setOnlineStatus(Integer v){onlineStatus=v;}
    public Integer getRunningStatus(){return runningStatus;} public void setRunningStatus(Integer v){runningStatus=v;}
    public Integer getVersion(){return version;} public void setVersion(Integer v){version=v;}
    public LocalDateTime getCreateTime(){return createTime;} public void setCreateTime(LocalDateTime v){createTime=v;}
    public LocalDateTime getUpdateTime(){return updateTime;} public void setUpdateTime(LocalDateTime v){updateTime=v;}
    public Boolean getDeleted(){return deleted;} public void setDeleted(Boolean v){deleted=v;}
}
