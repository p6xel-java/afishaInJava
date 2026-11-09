package com.example.afisha.dto;


import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class EventRequestDto {

    @NotBlank(message = "Название мероприятия обязательно")
    @Size(max = 255, message = "Название не должно превышать 255 символов")
    private String title;

    private String description;

    @NotBlank(message = "Место проведения мероприятия обязательно")
    @Size(max = 255, message = "Место проведения не должно превышать 255 символов")
    private String venue;

    @NotNull(message = "Стоймость обязательна")
    @DecimalMin(value = "0.0", message = "Стоймость не может быть отрицательной")
    private BigDecimal price;

    @NotNull(message = "Дата создания мероприятия обязательна")
    @FutureOrPresent(message = "Дата создания должна быть или в настоящем или будущем")
    private LocalDateTime startDateTime;
}
