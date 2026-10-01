package com.example.afisha.service;

import com.example.afisha.dto.EventResponseDto;
import com.example.afisha.dto.KudaGoEventDto;
import com.example.afisha.entity.Event;
import com.example.afisha.repository.EventRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;

    private final EventFetcherService eventFetcherService;

    public EventServiceImpl(EventRepository eventRepository, EventFetcherService eventFetcherService) {
        this.eventRepository = eventRepository;
        this.eventFetcherService = eventFetcherService;
    }

    @Override
    public void syncEvents() {
        List<KudaGoEventDto> externalEvents = eventFetcherService.fetchEventsFromExternalApi();

        for (KudaGoEventDto externalDto: externalEvents) {
            String externalIdStr = String.valueOf(externalDto.getId());

            if (!eventRepository.existsByExternalId(externalIdStr)) {
                Event event = new Event();
                event.setExternalId(externalIdStr);
                event.setTitle(externalDto.getTitle());
                event.setDescription(externalDto.getDescription());

                eventRepository.save(event);
            }
        }
    }

    @Override
    public List<EventResponseDto> getAllEvents() {
        return eventRepository.findAll().stream()
                .map(this::mapToResponseDto).toList();
    }

    @Override
    public EventResponseDto getEventById(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Событие с " + id + " не найдено"));
        return mapToResponseDto(event);
    }

    private EventResponseDto mapToResponseDto(Event event) {
        EventResponseDto dto = new EventResponseDto();
        dto.setId(event.getId());
        dto.setTitle(event.getTitle());
        dto.setDescription(event.getDescription());
        dto.setVenue(event.getVenue());
        dto.setPrice(event.getPrice());
        dto.setStartDateTime(event.getStartDateTime());
        return dto;
    }
}
