package com.factory.events.entity;

import java.time.Instant;

import com.factory.events.dto.EventDTO;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

@Data
@Entity
@Table(name = "events",
    uniqueConstraints = @UniqueConstraint(columnNames = "eventId")
)
public class Event {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(nullable = false)
    private String eventId;

    @Column(nullable = false)
    private Instant eventTime;

    @Column(nullable = false)
    private Instant receivedTime;

    @Column(nullable = false)
    private String machineId;

    @Column(nullable = false)
    private String lineId;

    @Column(nullable = false)
    private String factoryId;

    private Long durationMs;

    private Integer defectCount;


    public EventDTO toDTO() {
       return new EventDTO(this.eventId, this.eventTime, this.machineId, this.lineId, this.factoryId, this.durationMs,this.defectCount);
    }



    // Getters and Setters (if not using Lombok)
}
