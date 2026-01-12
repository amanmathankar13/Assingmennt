package com.factory.events.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopDefectLineResponse {
    private String lineId;
    private Long totalDefects;
    private Long eventCount;
    private Double defectPercent;
}
