package com.example.afisha.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Данные для создания мероприятия")
public class EventRequestDto {

    @NotBlank(message = "Название мероприятия обязательно")
    @Size(max = 255, message = "Название не должно превышать 255 символов")
    @Schema(description = "Название мероприятия", example = "Концерт")
    private String title;


    @Schema(description = "Описание мероприятия", example = "Концерт Kayne West")
    private String description;


    @NotBlank(message = "Место проведения мероприятия обязательно")
    @Size(max = 255, message = "Место проведения не должно превышать 255 символов")
    @Schema(description = "Место проведения мероприятия", example = "Газпром Арена")
    private String venue;


    @NotNull(message = "Стоймость обязательна")
    @DecimalMin(value = "0.0", message = "Стоймость не может быть отрицательной")
    @Schema(description = "Цена мероприятия", example = "1000")
    private BigDecimal price;


    @NotNull(message = "Дата создания мероприятия обязательна")
    @FutureOrPresent(message = "Дата создания должна быть или в настоящем или будущем")
    @Schema(description = "Время начала мероприятия", example = "2026-12-15T19:30:00")
    private LocalDateTime startDateTime;
}
