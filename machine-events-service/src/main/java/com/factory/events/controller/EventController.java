package com.factory.events.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.factory.events.dto.BatchResponse;
import com.factory.events.dto.EventDTO;
import com.factory.events.service.EventService;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping("/batch")
    public ResponseEntity<BatchResponse> ingestEvent(@RequestBody List<EventDTO> events) {
        return new ResponseEntity<>(eventService.ingestBatch(events), HttpStatus.CREATED);
    }
    

}
