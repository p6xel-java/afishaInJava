package com.example.afisha.controller;

import com.example.afisha.dto.EventResponseDto;
import com.example.afisha.repository.EventRepository;
import com.example.afisha.service.EventService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }


    @PostMapping("/sync")
    public String syncEvents() {
        eventService.syncEvents();
        return "sync completed";
    }

    @GetMapping
    public List<EventResponseDto> getAllEvents() {
        return eventService.getAllEvents();
    }

    @GetMapping("/{id}")
    public EventResponseDto getEventById(@PathVariable Long id) {
        return eventService.getEventById(id);
    }
}
