package com.example.afisha.controller;


import com.example.afisha.entity.Ticket;
import com.example.afisha.dto.TicketResponseDto;
import com.example.afisha.entity.TicketStatus;
import com.example.afisha.repository.TicketRepository;
import com.example.afisha.service.EventService;
import com.example.afisha.service.TicketTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
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
@Tag(name = "Билеты", description = "Выводит информацию о мероприятии по купленному билету")
public class TicketController {

    private final TicketRepository tickets;
    private final TicketTokenService tokens;
    private final EventService events;

    @GetMapping("/{token}")
    @Operation(summary = "Информация о купленном билете", description = "Выводит полную информацию о мероприятии по купленному билету")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Информация об активном билете успешно получена",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TicketResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Билет не найден или не активирован",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "Ошибка 404", value = """
                {
                  "timestamp": "2026-11-16T19:30:00",
                  "status": 404,
                  "error": "Not Found",
                  "message": "Ресурс не найден"
                }
                """))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "Ошибка 500", value = """
                {
                  "timestamp": "2026-11-16T19:30:00",
                  "status": 500,
                  "error": "Internal Server Error",
                  "message": "Внутренняя ошибка сервера"
                }
                """)))
    })
    public TicketResponseDto getTicket(@Parameter(description = "Токен купленного билета") @PathVariable("token") String token) {
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
