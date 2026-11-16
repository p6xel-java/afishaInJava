package com.example.afisha.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Информация полученная о приобретенном билете на мероприятие")
public class TicketResponseDto {

    @Schema(description = "ID билета в базе данных", example = "2")
    private Long ticketId;


    @Schema(description = "Статус билета", example = "PENDING, PAID")
    private String status;


    @Schema(description = "Время покупки билета", example = "2026-11-15T18:24:49.161943Z")
    private Instant paidAt;

    @Schema(description = "Объект мероприятие")
    private EventResponseDto event;

}
