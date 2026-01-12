package com.factory.events.service;

import java.time.Instant;
import java.util.List;

import com.factory.events.dto.StatsResponse;
import com.factory.events.dto.TopDefectLineResponse;

public interface StatsService {

    public StatsResponse getStats(String machineId, Instant start, Instant end);

    public List<TopDefectLineResponse> getTopDefectLine(String machineId, Instant start, Instant end, Integer topN);
}
