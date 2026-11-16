package com.example.afisha.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Информация о мероприятии")
public class EventResponseDto {

    @Schema(description = "Внутренний ID", example = "3")
    private Long id;

    @Schema(description = "Внешний ID", nullable = true, example = "null")
    private String externalId;

    @Schema(description = "Название мероприятия", example = "Концерт")
    private String title;

    @Schema(description = "Описание мероприятия", example = "Концерт Kaney West")
    private String description;

    @Schema(description = "Место проведения мероприятия", example = "Газпром Арена")
    private String venue;

    @Schema(description = "Цена мероприятия", example = "666")
    private BigDecimal price;


    @Schema(description = "Время начала мероприятия", example = "2026-12-15T19:30:00")
    private LocalDateTime startDateTime;

}
