package com.factory.events.dto;

import java.time.Instant;

import com.factory.events.entity.Event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventDTO {
    private String eventId;
    private Instant eventTime;
    private String machineId;
    private String lineId;
    private String factoryId;
    private Long durationMs;
    private Integer defectCount;

    public Event toEntity() {
        Event event = new Event();
        event.setEventId(this.eventId);
        event.setEventTime(this.eventTime);
        event.setMachineId(this.machineId);
        event.setDurationMs(this.durationMs != null ? this.durationMs : 0L);
        event.setDefectCount(this.defectCount != null ? this.defectCount : 0);
        event.setLineId(this.lineId);
        event.setFactoryId(this.factoryId);
        event.setReceivedTime(Instant.now());
        return event;
    }


    // Client cannot send recievedTime thats why it is missing here
}
