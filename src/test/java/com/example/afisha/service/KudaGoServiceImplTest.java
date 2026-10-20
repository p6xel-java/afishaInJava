package com.example.afisha.service;

import com.example.afisha.dto.kudago.KudaGoEventDto;
import com.example.afisha.dto.kudago.KudaGoResponceDto;
import com.example.afisha.entity.Event;
import com.example.afisha.repository.EventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KudaGoServiceImplTest {

    @Mock
    private RestClient kudaGoRestClient;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private RestClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @InjectMocks
    private KudaGoServiceImpl kudaGoService;



    @Test
    void successImportEventsTest() {

        KudaGoEventDto eventDto = new KudaGoEventDto();
        eventDto.setId(123L);
        eventDto.setTitle("Концерт Kanye West");
        eventDto.setDescription("Супер концерт, которого никогда не будет");
        eventDto.setPrice("100000");

        KudaGoResponceDto responceDto = new KudaGoResponceDto();
        responceDto.setCount(1);
        responceDto.setResults(List.of(eventDto));

        when(kudaGoRestClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(KudaGoResponceDto.class)).thenReturn(responceDto);

        when(eventRepository.findByExternalId("123")).thenReturn(Optional.empty());
        int result = kudaGoService.importEventsFromKudaGo("spb",1);
        assertEquals(1, result);


        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository, times(1)).save(eventCaptor.capture());

        Event savedEvent = eventCaptor.getValue();
        assertEquals(eventDto.getId().toString(), savedEvent.getExternalId());
        assertEquals(eventDto.getTitle(), savedEvent.getTitle());
        assertEquals(eventDto.getDescription(), savedEvent.getDescription());
        assertEquals(eventDto.getPrice(), savedEvent.getPrice().toString());
    }

    @Test
    void importEventAndUpdateExistingEventTest() {
        KudaGoEventDto eventDto = new KudaGoEventDto();
        eventDto.setId(999L);
        eventDto.setTitle("Обновленное название");
        eventDto.setDescription("Обновленное описание");
        eventDto.setPrice("0");

        KudaGoResponceDto responceDto = new KudaGoResponceDto();
        responceDto.setResults(List.of(eventDto));

        when(kudaGoRestClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(KudaGoResponceDto.class)).thenReturn(responceDto);

        Event existingEvent = new Event();
        existingEvent.setId(10L);
        existingEvent.setExternalId("999");
        when(eventRepository.findByExternalId("999")).thenReturn(Optional.of(existingEvent));

        int importedCount = kudaGoService.importEventsFromKudaGo("spb", 1);

        assertEquals(1, importedCount);

        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository, times(1)).save(eventCaptor.capture());

        Event savedEvent = eventCaptor.getValue();
        assertEquals(10L, savedEvent.getId());
        assertEquals(BigDecimal.ZERO, savedEvent.getPrice());
    }

}