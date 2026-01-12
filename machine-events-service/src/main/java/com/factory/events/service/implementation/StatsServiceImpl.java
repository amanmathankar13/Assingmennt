package com.factory.events.service.implementation;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.factory.events.dto.StatsResponse;
import com.factory.events.dto.Status;
import com.factory.events.dto.TopDefectLineResponse;
import com.factory.events.entity.Event;
import com.factory.events.repository.EventRepository;
import com.factory.events.service.StatsService;

@Service
public class StatsServiceImpl implements StatsService {

    private final EventRepository eventRepository;

    public StatsServiceImpl(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Override
    public StatsResponse getStats(String machineId, Instant start, Instant end) {
        List<Event> events = eventRepository.findByMachineIdAndEventTimeBetween(machineId, start, end);
        Long eventCount = (long)events.size();
        Integer defectCount = events.stream().mapToInt(Event::getDefectCount).sum();
        Double hours = Duration.between(start, end).toSeconds() / 3600.0;
        Double averageDefectRate = hours > 0 ? defectCount / hours : 0.0;
        return new StatsResponse(machineId, start, end, eventCount, defectCount, averageDefectRate,
                                averageDefectRate < 2.0 ? Status.HEALTHY : Status.WARNING);
    }

    @Override
    public List<TopDefectLineResponse> getTopDefectLine(String machineId, Instant start, Instant end, Integer topN) {
        // Implementation depends on the definition of TopDefectLineDto and the data model.
        // Assuming we have a method in EventRepository to fetch top defect lines.
        List<TopDefectLineResponse> topDefectLinesDto = eventRepository.findTopDefectLines(
            machineId, start, end, PageRequest.of(0, topN)
        ).stream().map(dto -> new TopDefectLineResponse(dto.getLineId(), dto.getTotalDefects(), dto.getEventCount(), dto.getDefectPercent()))
         .toList();
        return topDefectLinesDto;
    }
    
}
