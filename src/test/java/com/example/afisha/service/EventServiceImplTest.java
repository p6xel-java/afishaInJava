package com.example.afisha.service;

import com.example.afisha.dto.EventRequestDto;
import com.example.afisha.dto.EventResponseDto;
import com.example.afisha.dto.kudago.KudaGoEventDto;
import com.example.afisha.entity.Event;
import com.example.afisha.repository.EventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {


    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventServiceImpl eventService;

    @Mock
    private EventFetcherService eventFetcherService;


    @Test
    void getAllEventsReturnsEmptyList() {
        when(eventRepository.findAll()).thenReturn(List.of());
        assertEquals(true, eventService.getAllEvents().isEmpty());
    }

    @Test
    void getEventByIdReturnedMappedEvent() {
        when(eventRepository.findById(5L)).thenReturn(Optional.of(exampleEvent(5L)));
        assertEquals(new BigDecimal(1000), eventService.getEventById(5L).getPrice());
    }

    @Test
    void getAllEventsMapsEntityFields() {
        Event event = exampleEvent(5L);
        when(eventRepository.findAll()).thenReturn(List.of(event));

        List<EventResponseDto> result = eventService.getAllEvents();
        assertEquals(1, result.size());

        EventResponseDto dto = result.get(0);
        assertEquals(5L, dto.getId());
        assertEquals("Concert", dto.getTitle());
        assertEquals("Some description", dto.getDescription());
        assertEquals("Zenit Area", dto.getVenue());
        assertEquals(new BigDecimal(1000), dto.getPrice());
        assertEquals(LocalDateTime.of(2007, 06, 27, 15, 30), dto.getStartDateTime());
    }

    @Test
    void syncSavesOnlyEventsThatDoNotAlreadyExist() {
        KudaGoEventDto newEvent = externalEvent(100L, "New");
        KudaGoEventDto oldEvent = externalEvent(200L, "Old");

        when(eventFetcherService.fetchEventsFromExternalApi()).thenReturn(List.of(newEvent, oldEvent));
        when(eventRepository.existsByExternalId("100")).thenReturn(false);
        when(eventRepository.existsByExternalId("200")).thenReturn(true);

        eventService.syncEvents();

        ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository).save(captor.capture());

        assertEquals("100", captor.getValue().getExternalId());
        assertEquals("New", captor.getValue().getTitle());
        assertEquals("Description New", captor.getValue().getDescription());
        verify(eventRepository).existsByExternalId("200");
    }

    @Test
    void syncWithEmptyExternalResponseDoesNotWriteToDatabase() {
        when(eventFetcherService.fetchEventsFromExternalApi()).thenReturn(List.of());
        eventService.syncEvents();
        verify(eventRepository, never()).save(any());
    }

    @Test
    void createEventTransfersRequestToEntityAndReturnsSavedData() {
        EventRequestDto request = new EventRequestDto();
        request.setTitle("Concert");
        request.setDescription("Some description");
        request.setVenue("Zenit Area");
        request.setPrice(new BigDecimal(1000));
        request.setStartDateTime(LocalDateTime.of(2007, 06, 27, 15, 30));

        when(eventRepository.save(any(Event.class))).thenAnswer(invocationOnMock -> {
            Event saved = invocationOnMock.getArgument(0);
            saved.setId(5L);
            return saved;
        });

        EventResponseDto responseDto = eventService.createEvent(request);

        ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository).save(captor.capture());

        assertEquals("Concert", captor.getValue().getTitle());
        assertEquals("Some description", captor.getValue().getDescription());
        assertEquals("Zenit Area", captor.getValue().getVenue());
        assertEquals(new BigDecimal(1000), captor.getValue().getPrice());
        assertEquals(request.getStartDateTime(), captor.getValue().getStartDateTime());
        assertEquals(5L, responseDto.getId());
    }

    private Event exampleEvent(Long id) {
        Event event = new Event();
        event.setId(id);
        event.setTitle("Concert");
        event.setDescription("Some description");
        event.setVenue("Zenit Area");
        event.setPrice(new BigDecimal(1000));
        event.setStartDateTime(LocalDateTime.of(2007, 06, 27, 15, 30));
        return event;
    }

    private KudaGoEventDto externalEvent(Long id, String title) {
        KudaGoEventDto dto = new KudaGoEventDto();
        dto.setId(id);
        dto.setTitle(title);
        dto.setDescription("Description " + title);
        return dto;
    }

}