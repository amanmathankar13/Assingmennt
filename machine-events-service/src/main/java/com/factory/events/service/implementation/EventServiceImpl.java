package com.factory.events.service.implementation;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.factory.events.dto.BatchResponse;
import com.factory.events.dto.EventDTO;
import com.factory.events.entity.Event;
import com.factory.events.exception.ValidateException;
import com.factory.events.payload.IngestResult;
import com.factory.events.payload.Rejection;
import com.factory.events.repository.EventRepository;
import com.factory.events.service.EventService;

import jakarta.transaction.Transactional;


@Service
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;

        public EventServiceImpl(EventRepository eventRepository) {
            this.eventRepository = eventRepository;
        }


    @Override
    @Transactional
    public BatchResponse ingestBatch(List<EventDTO> events) {
        BatchResponse response = new BatchResponse();

        for(EventDTO event : events){
            try{
                IngestResult result = processEvent(event);
                switch(result){
                    case ACCEPTED:
                        response.setAccepted(response.getAccepted() + 1);
                        break;
                    case UPDATED:
                        response.setUpdated(response.getUpdated() + 1);
                        break;
                    case DEDUPED:
                        response.setDeduped(response.getDeduped() + 1);
                        break;
                }
            }
            catch(ValidateException e){
                response.setRejected(response.getRejected() + 1);
                response.getRejections().add(new Rejection(event.getEventId(), e.getMessage()));
                // Here we would normally add a Rejection object to the rejections list
                // but for brevity, this is omitted.
            }
        }
        return response;
    }

    
    public IngestResult processEvent(EventDTO eventDTO) {
        validateEventDTO(eventDTO);
        Optional<Event> existingEvent = eventRepository.findByEventId(eventDTO.getEventId());

        if(existingEvent.isEmpty()){
            eventRepository.save(eventDTO.toEntity());
            return IngestResult.ACCEPTED;
        }

        Event event = existingEvent.get();

        if(samePayload(event, eventDTO)){
            return IngestResult.DEDUPED;
        }

        if(Instant.now().isAfter(event.getReceivedTime())){
            Event updatedEvent = eventDTO.toEntity();
            updatedEvent.setId(event.getId());
            updatedEvent.setReceivedTime(Instant.now());
            eventRepository.save(updatedEvent);
            return IngestResult.UPDATED;
        }
        return IngestResult.DEDUPED;
    }

    
    public void validateEventDTO(EventDTO eventDTO) {
        if(eventDTO.getDurationMs() < 0 || eventDTO.getDurationMs() > 6*60*60*1000){
            throw new ValidateException("INVALID_DURATION");
        }

        if(eventDTO.getEventTime().isAfter(Instant.now().plus(15, ChronoUnit.MINUTES))){
            throw new ValidateException("EVENT_TIME_IN_FUTURE");
        }
    }

    
    public Boolean samePayload(Event existingEvent, EventDTO newEvent) {
        return existingEvent.getEventTime().equals(newEvent.getEventTime()) &&
               existingEvent.getMachineId().equals(newEvent.getMachineId()) &&
               existingEvent.getDurationMs().equals(newEvent.getDurationMs()) &&
               existingEvent.getDefectCount().equals(newEvent.getDefectCount());
    }


    @Override
    public EventDTO getById(String eventId) {
        Optional<Event> eventOpt = eventRepository.findByEventId(eventId);
        if(eventOpt.isPresent()){
            return eventOpt.get().toDTO();
        }
        return null;
    }
    
}
