package com.example.afisha.controller;

import com.example.afisha.dto.EventRequestDto;
import com.example.afisha.dto.EventResponseDto;
import com.example.afisha.service.EventService;
import com.example.afisha.service.KudaGoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class EventControllerTest {

    @Mock
    private EventService eventService;

    @Mock
    private KudaGoService kudaGoService;

    @InjectMocks
    private EventController eventController;

    @Test
    void syncCallsService() {
        assertEquals("sync completed", eventController.syncEvents());
        verify(eventService).syncEvents();
    }

    @Test
    void getAllReturnsServiceResults() {
        List<EventResponseDto> expect = eventService.getAllEvents();
        when(eventService.getAllEvents()).thenReturn(expect);
        assertSame(expect, eventController.getAllEvents());
    }

    @Test
    void getOnePassesId() {
        EventResponseDto expect = new EventResponseDto();
        when(eventService.getEventById(5L)).thenReturn(expect);
        assertSame(expect, eventController.getEventById(5L));
        verify(eventService).getEventById(5L);
    }

    @Test
    void createPassesDto() {
        EventRequestDto request = new EventRequestDto();
        EventResponseDto response = new EventResponseDto();
        when(eventService.createEvent(request)).thenReturn(response);
        assertSame(response, eventController.createEvent(request));
        verify(eventService).createEvent(request);
    }

    @Test
    void importReturnsCountAddSuccessStatus() {
        when(kudaGoService.importEventsFromKudaGo("spb", 2)).thenReturn(36);
        ResponseEntity<Map<String, Object>> response = eventController.importEvents("spb", 2);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(36, response.getBody().get("importedCount"));
        assertTrue(response.getBody().get("message") instanceof String);
        verify(kudaGoService).importEventsFromKudaGo("spb", 2);
    }


}