package com.factory.events.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatsResponse {

    private String machineId;
    private Instant startTime;
    private Instant endTime;
    private Long eventCount;
    private Integer defectCount;
    private Double averageDefectRate;
    private Status status;
}
