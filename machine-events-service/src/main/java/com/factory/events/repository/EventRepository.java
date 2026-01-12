package com.factory.events.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.factory.events.dto.TopDefectLineDto;
import com.factory.events.entity.Event;

public interface EventRepository extends JpaRepository<Event, Long> {

    Optional<Event> findByEventId(String eventId);

    List<Event> findByMachineIdAndEventTimeBetween(String machineId, Instant start, Instant end);

     @Query("""
        SELECT new com.factory.events.dto.TopDefectLineDto(
            e.lineId,
            SUM(COALESCE(e.defectCount, 0)),
            COUNT(e)
        )
        FROM Event e
        WHERE e.factoryId = :factoryId
        AND e.eventTime >= :from
        AND e.eventTime < :to
        GROUP BY e.lineId
        ORDER BY SUM(COALESCE(e.defectCount, 0)) DESC
    """)
    List<TopDefectLineDto> findTopDefectLines(
        String factoryId,
        Instant from,
        Instant to,
        Pageable pageable
    );

}
