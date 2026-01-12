package com.factory.events.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopDefectLineDto {

    private String lineId;

    private Long totalDefects;

    private Long eventCount;


    public Double getDefectPercent() {
        if (eventCount == 0) {
            return 0.0;
        }
        return (totalDefects.doubleValue() * 10000.0 / eventCount.doubleValue()) * 100;
    }
}
