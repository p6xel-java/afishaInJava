package com.example.afisha.controller;

import com.example.afisha.dto.EventRequestDto;
import com.example.afisha.dto.EventResponseDto;
import com.example.afisha.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;


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

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public EventResponseDto createEvent(@RequestBody EventRequestDto requestDto) {
        return eventService.createEvent(requestDto);
    }


}
