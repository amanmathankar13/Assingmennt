package com.factory.events.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.factory.events.dto.StatsResponse;
import com.factory.events.dto.TopDefectLineResponse;
import com.factory.events.service.StatsService;

import java.time.Instant;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/stats")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping
    public ResponseEntity<StatsResponse> getMethodName(@RequestParam String machineId, @RequestParam Instant start, @RequestParam Instant end) {
        return ResponseEntity.ok(statsService.getStats(machineId, start, end));
    }

    @GetMapping("/top-defect-lines")
    public ResponseEntity<List<TopDefectLineResponse>> getTopDefectLines(
            @RequestParam String machineId,
            @RequestParam Instant start,
            @RequestParam Instant end,
            @RequestParam Integer topN) {
        return ResponseEntity.ok(statsService.getTopDefectLine(machineId, start, end, topN));
    }
    
    
}
