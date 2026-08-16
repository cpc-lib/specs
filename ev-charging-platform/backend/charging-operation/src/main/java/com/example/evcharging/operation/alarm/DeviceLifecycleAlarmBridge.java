package com.example.evcharging.operation.alarm;

import com.example.evcharging.framework.contract.DeviceAlarmEvent;
import com.example.evcharging.framework.contract.DeviceLifecycleEvent;
import com.example.evcharging.framework.event.DomainEventEnvelope;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Objects;

@Component
public class DeviceLifecycleAlarmBridge {
    private final ObjectMapper mapper;
    private final KafkaTemplate<String,String> kafka;
    private final String alarmTopic;

    public DeviceLifecycleAlarmBridge(
            ObjectMapper mapper,
            KafkaTemplate<String,String> kafka,
            @Value("${charging.operation.alarm-topic:ev.device.alarm.v1}") String alarmTopic) {
        this.mapper=mapper;this.kafka=kafka;this.alarmTopic=alarmTopic;
    }

    @KafkaListener(topics="${charging.operation.lifecycle-topic:ev.device.lifecycle.v1}",groupId="charging-operation-lifecycle-v1")
    public void consume(String raw) throws Exception {
        DomainEventEnvelope<DeviceLifecycleEvent> source =
                mapper.readValue(raw,new TypeReference<DomainEventEnvelope<DeviceLifecycleEvent>>(){});
        DeviceLifecycleEvent event=Objects.requireNonNull(source.payload(),"lifecycle payload required");
        if(event.tenantId()!=source.tenantId()) throw new IllegalArgumentException("tenant mismatch");

        String alarmEventType;
        DeviceAlarmEvent alarm;
        if("OFFLINE".equalsIgnoreCase(event.eventType())) {
            alarmEventType="iot.device.alarm.raised";
            alarm=new DeviceAlarmEvent(
                    event.tenantId(),"RAISED",event.deviceId(),null,
                    "DEVICE_OFFLINE","MAJOR",null,null,
                    "Device heartbeat timed out: "+event.reason(),
                    event.occurredAt()!=null?event.occurredAt():Instant.now());
        } else if("ONLINE".equalsIgnoreCase(event.eventType())) {
            alarmEventType="iot.device.alarm.recovered";
            alarm=new DeviceAlarmEvent(
                    event.tenantId(),"RECOVERED",event.deviceId(),null,
                    "DEVICE_OFFLINE","INFO",null,null,
                    "Device reconnected",event.occurredAt()!=null?event.occurredAt():Instant.now());
        } else {
            return;
        }

        String eventId="lifecycle-alarm:"+source.eventId();
        var envelope=new DomainEventEnvelope<>(
                eventId,alarmEventType,"1.0","Device",event.deviceId(),event.tenantId(),
                source.traceId(),Instant.now(),"charging-operation",alarm);
        kafka.send(alarmTopic,event.deviceId(),mapper.writeValueAsString(envelope));
    }
}
