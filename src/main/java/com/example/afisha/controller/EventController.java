package com.example.afisha.controller;

import com.example.afisha.dto.EventRequestDto;
import com.example.afisha.dto.EventResponseDto;
import com.example.afisha.service.EventService;
import com.example.afisha.service.KudaGoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    private final KudaGoService kudaGoService;


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
    public EventResponseDto getEventById(@PathVariable @Min(value = 1, message = "ID должно быть положительным") Long id) {
        return eventService.getEventById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public EventResponseDto createEvent(@RequestBody @Valid EventRequestDto requestDto) {
        return eventService.createEvent(requestDto);
    }

    @PostMapping("/import")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> importEvents(
            @RequestParam(defaultValue = "spb") @NotBlank String location,
            @RequestParam(defaultValue = "1") @Min(1) @Max(100) int pages) {
        int count = kudaGoService.importEventsFromKudaGo(location, pages);
        return ResponseEntity.ok(Map.of(
                "message", "Импорт из KudeGo завершен успешно", "importedCount", count
        ));
    }


}
