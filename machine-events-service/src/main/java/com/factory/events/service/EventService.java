package com.factory.events.service;

import java.util.List;

import com.factory.events.dto.BatchResponse;
import com.factory.events.dto.EventDTO;


public interface EventService {

    public BatchResponse ingestBatch(List<EventDTO> events);

    public EventDTO getById(String eventId);

}
