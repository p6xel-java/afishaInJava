package com.example.afisha.controller;

import com.example.afisha.dto.EventResponseDto;
import com.example.afisha.dto.TicketResponseDto;
import com.example.afisha.entity.Event;
import com.example.afisha.entity.Ticket;
import com.example.afisha.entity.TicketStatus;
import com.example.afisha.repository.TicketRepository;
import com.example.afisha.service.EventService;
import com.example.afisha.service.TicketTokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketControllerTest {

    @Mock
    private TicketRepository tickets;
    @Mock
    private TicketTokenService tokens;
    @Mock
    private EventService events;
    @InjectMocks
    private TicketController controller;

    private static final String TOKEN = "a".repeat(43);


    @Test
    void rejectsMalformedTokenBeforeDatabaseAccess() {
        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> controller.getTicket("invalid"));
        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
        verifyNoInteractions(tickets, tokens, events);
    }

    @Test
    void rejectsUnknownTicket() {
        when(tokens.hash(TOKEN)).thenReturn("hashed");
        when(tickets.findByTicketTokenHash("hashed")).thenReturn(Optional.empty());
        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> controller.getTicket(TOKEN));
        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
        verifyNoInteractions(events);
    }

    @Test
    void rejectsUnpaidTicket() {
        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.PENDING);
        when(tokens.hash(TOKEN)).thenReturn("hashed");
        when(tickets.findByTicketTokenHash("hashed")).thenReturn(Optional.of(ticket));
        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> controller.getTicket(TOKEN));
        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
        verifyNoInteractions(events);
    }

    @Test
    void returnsPaidTicketDetails() {
        Event event = new Event(); event.setId(9L);
        Ticket ticket = new Ticket(); ticket.setId(12L); ticket.setEvent(event);
        ticket.setStatus(TicketStatus.PAID);
        Instant paidAt = Instant.parse("2026-01-01T10:00:00Z"); ticket.setPaidAt(paidAt);
        when(tokens.hash(TOKEN)).thenReturn("hashed");
        when(tickets.findByTicketTokenHash("hashed")).thenReturn(Optional.of(ticket));
        EventResponseDto eventDto = mock(EventResponseDto.class);
        when(events.getEventById(9L)).thenReturn(eventDto);
        TicketResponseDto response = controller.getTicket(TOKEN);
        assertEquals(12L, response.getTicketId());
        assertEquals("PAID", response.getStatus());
        assertEquals(paidAt, response.getPaidAt());
        assertSame(eventDto, response.getEvent());
    }

}