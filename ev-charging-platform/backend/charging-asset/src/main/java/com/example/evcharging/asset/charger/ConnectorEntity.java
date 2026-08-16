package com.example.evcharging.asset.charger;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

@TableName("charger_connector")
public class ConnectorEntity {
    @TableId(type = IdType.INPUT) private Long id;
    private Long tenantId;
    private Long stationId;
    private Long chargerId;
    private String connectorCode;
    private Integer connectorNo;
    private Integer connectorType;
    private Long ratedPowerW;
    private Integer onlineStatus;
    private Integer runningStatus;
    @Version private Integer version;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic private Boolean deleted;
    public Long getId(){return id;} public void setId(Long v){id=v;}
    public Long getTenantId(){return tenantId;} public void setTenantId(Long v){tenantId=v;}
    public Long getStationId(){return stationId;} public void setStationId(Long v){stationId=v;}
    public Long getChargerId(){return chargerId;} public void setChargerId(Long v){chargerId=v;}
    public String getConnectorCode(){return connectorCode;} public void setConnectorCode(String v){connectorCode=v;}
    public Integer getConnectorNo(){return connectorNo;} public void setConnectorNo(Integer v){connectorNo=v;}
    public Integer getConnectorType(){return connectorType;} public void setConnectorType(Integer v){connectorType=v;}
    public Long getRatedPowerW(){return ratedPowerW;} public void setRatedPowerW(Long v){ratedPowerW=v;}
    public Integer getOnlineStatus(){return onlineStatus;} public void setOnlineStatus(Integer v){onlineStatus=v;}
    public Integer getRunningStatus(){return runningStatus;} public void setRunningStatus(Integer v){runningStatus=v;}
    public Integer getVersion(){return version;} public void setVersion(Integer v){version=v;}
    public LocalDateTime getCreateTime(){return createTime;} public void setCreateTime(LocalDateTime v){createTime=v;}
    public LocalDateTime getUpdateTime(){return updateTime;} public void setUpdateTime(LocalDateTime v){updateTime=v;}
    public Boolean getDeleted(){return deleted;} public void setDeleted(Boolean v){deleted=v;}
}
