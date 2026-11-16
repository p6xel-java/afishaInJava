package com.example.afisha.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Данные для создания платежа")
public class PaymentInitRequest {

    @NotNull
    @Min(1)
    @Schema(description = "ID мероприятия", example = "4")
    private Long eventId;
}
