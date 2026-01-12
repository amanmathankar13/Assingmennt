package com.factory.events;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.assertEquals;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.factory.events.dto.EventDTO;
import com.factory.events.entity.Event;
import com.factory.events.repository.EventRepository;
import com.factory.events.service.EventService;
import com.factory.events.service.StatsService;

@SpringBootTest
public class MachineEventsServiceApplicationTests {

    @Autowired
    private EventService eventService;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private StatsService statsService;

    private EventDTO baseEventDTO;

    @BeforeEach
    void setup() {
        eventRepository.deleteAll();
        baseEventDTO = new EventDTO();
        baseEventDTO.setEventId("E-100");
        baseEventDTO.setMachineId("M-001");
        baseEventDTO.setLineId("L-001");
        baseEventDTO.setFactoryId("F-001");
        baseEventDTO.setDurationMs(1000L);
        baseEventDTO.setDefectCount(2);
        baseEventDTO.setEventTime(Instant.now());
    }

    /** 1. Identical duplicate eventId → deduped */
    @Test
    void testDuplicateEventIdDeduped() {
        List<EventDTO> events = new ArrayList<>();
        events.add(baseEventDTO);
        events.add(baseEventDTO); // duplicate

        var result = eventService.ingestBatch(events);

        assertEquals(1, result.getAccepted());
        assertEquals(1, result.getDeduped());
    }

    /** 2. Different payload + newer receivedTime → update happens */
    @Test
    void testUpdateWithNewerReceivedTime() throws InterruptedException {
        eventService.ingestBatch(List.of(baseEventDTO));

        EventDTO updatedEventDTO = new EventDTO();
        updatedEventDTO.setEventId("E-100");
        updatedEventDTO.setMachineId("M-001");
        updatedEventDTO.setLineId("L-001");
        updatedEventDTO.setFactoryId("F-001");
        updatedEventDTO.setDurationMs(2000L);
        updatedEventDTO.setDefectCount(5);
        updatedEventDTO.setEventTime(baseEventDTO.getEventTime());

        // System will set a later receivedTime internally

        var result = eventService.ingestBatch(List.of(updatedEventDTO));

        assertEquals(0, result.getAccepted());
        assertEquals(1, result.getUpdated());

        EventDTO eventDTO = eventService.getById("E-100");
        Event stored = eventDTO.toEntity();
        assertEquals(2000L, stored.getDurationMs());
        assertEquals(5, stored.getDefectCount());
    }

    /** 3. Different payload + older receivedTime → ignored */
    @Test
    void testIgnoreOlderReceivedTime() {
        eventService.ingestBatch(List.of(baseEventDTO));

        EventDTO olderDTO = new EventDTO();
        olderDTO.setEventId("E-100");
        olderDTO.setMachineId("M-001");
        olderDTO.setLineId("L-001");
        olderDTO.setFactoryId("F-001");
        olderDTO.setDurationMs(3000L);
        olderDTO.setDefectCount(10);
        olderDTO.setEventTime(baseEventDTO.getEventTime().plusSeconds(60*16));

        var result = eventService.ingestBatch(List.of(olderDTO));

        assertEquals(0, result.getAccepted());
        assertEquals(0, result.getUpdated());
        assertEquals(1, result.getRejected());
    }

    /** 4. Invalid duration rejected */
    @Test
    void testInvalidDurationRejected() {
        EventDTO invalidDTO = new EventDTO();
        invalidDTO.setEventId("E-101");
        invalidDTO.setMachineId("M-001");
        invalidDTO.setLineId("L-001");
        invalidDTO.setFactoryId("F-001");
        invalidDTO.setDurationMs(-50L); // invalid
        invalidDTO.setDefectCount(1);
        invalidDTO.setEventTime(Instant.now());

        var result = eventService.ingestBatch(List.of(invalidDTO));

        assertEquals(0, result.getAccepted());
        assertEquals(1, result.getRejected());
        assertEquals("INVALID_DURATION", result.getRejections().get(0).getReason());
    }

    /** 5. Future eventTime rejected (> 15 min) */
    @Test
    void testFutureEventRejected() {
        EventDTO futureDTO = new EventDTO();
        futureDTO.setEventId("E-102");
        futureDTO.setMachineId("M-001");
        futureDTO.setLineId("L-001");
        futureDTO.setFactoryId("F-001");
        futureDTO.setDurationMs(1000L);
        futureDTO.setDefectCount(0);
        futureDTO.setEventTime(Instant.now().plusSeconds(16 * 60)); // 16 min in future

        var result = eventService.ingestBatch(List.of(futureDTO));

        assertEquals(0, result.getAccepted());
        assertEquals(1, result.getRejected());
        assertEquals("EVENT_TIME_IN_FUTURE", result.getRejections().get(0).getReason());
    }

    /** 6. defectCount = -1 ignored in defect totals */
    @Test
    void testDefectCountIgnored() {
        EventDTO unknownDefectDTO = new EventDTO();
        unknownDefectDTO.setEventId("E-103");
        unknownDefectDTO.setMachineId("M-001");
        unknownDefectDTO.setLineId("L-001");
        unknownDefectDTO.setFactoryId("F-001");
        unknownDefectDTO.setDurationMs(1000L);
        unknownDefectDTO.setDefectCount(-1);
        unknownDefectDTO.setEventTime(Instant.now());

        eventService.ingestBatch(List.of(baseEventDTO, unknownDefectDTO));

        var stats = statsService.getStats("M-001", baseEventDTO.getEventTime(), Instant.now().plusSeconds(1));
        assertEquals(2, stats.getEventCount());
        assertEquals(1, stats.getDefectCount()); // only baseEvent counted
    }

    /** 7. start/end boundary correctness (inclusive/exclusive) */
    @Test
    void testStartEndBoundary() {
        Instant start = Instant.now();
        Instant inside = start.plusSeconds(10);
        Instant end = start.plusSeconds(20);

        EventDTO e1 = new EventDTO();
        e1.setEventId("E-104");
        e1.setMachineId("M-001");
        e1.setLineId("L-001");
        e1.setFactoryId("F-001");
        e1.setDurationMs(500L);
        e1.setDefectCount(1);
        e1.setEventTime(inside);

        EventDTO e2 = new EventDTO();
        e2.setEventId("E-105");
        e2.setMachineId("M-001");
        e2.setLineId("L-001");
        e2.setFactoryId("F-001");
        e2.setDurationMs(500L);
        e2.setDefectCount(1);
        e2.setEventTime(end.plusSeconds(1)); // exclusive

        eventService.ingestBatch(List.of(e1, e2));

        var stats = statsService.getStats("M-001", start, end);

        assertEquals(1, stats.getEventCount()); // e1 included, e2 excluded
    }

    /** 8. Thread-safety test: concurrent ingestion doesn’t corrupt counts */
    @Test
    void testConcurrentIngestion() throws InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Callable<Void>> tasks = new ArrayList<>();

        for (int i = 0; i < 50; i++) {
            int finalI = i;
            tasks.add(() -> {
                EventDTO e = new EventDTO();
                e.setEventId("EC-" + finalI);
                e.setMachineId("M-001");
                e.setLineId("L-001");
                e.setFactoryId("F-001");
                e.setDurationMs(1000L);
                e.setDefectCount(1);
                e.setEventTime(Instant.now());

                eventService.ingestBatch(List.of(e));
                return null;
            });
        }

        executor.invokeAll(tasks);
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        var stats = statsService.getStats("M-001", Instant.now().minusSeconds(60), Instant.now().plusSeconds(60));
        assertEquals(50, stats.getEventCount());
    }
}



