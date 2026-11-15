package com.example.afisha.controller;


import com.example.afisha.dto.Ticket;
import com.example.afisha.dto.TicketResponseDto;
import com.example.afisha.dto.TicketStatus;
import com.example.afisha.repository.TicketRepository;
import com.example.afisha.service.EventService;
import com.example.afisha.service.TicketTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketRepository tickets;
    private final TicketTokenService tokens;
    private final EventService events;

    @GetMapping("/{token}")
    public TicketResponseDto getTicket(@PathVariable("token") String token) {
        if (!token.matches("[A-Za-z0-9_-]{43}")) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Билет не найден");
        }

        Ticket ticket = tickets.findByTicketTokenHash(tokens.hash(token))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Билет не найден"));

        if (ticket.getStatus() != TicketStatus.PAID) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Билет не активирован");
        }

        return new TicketResponseDto(ticket.getId(),
                ticket.getStatus().name(),
                ticket.getPaidAt(),
                events.getEventById(ticket.getEvent().getId()));
    }
}
