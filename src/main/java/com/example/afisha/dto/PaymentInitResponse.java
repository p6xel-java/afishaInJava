package com.example.afisha.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Информация о созданном мероприятии")
public class PaymentInitResponse {

    @Schema(description = "ID платежа в базе данных", example = "12")
    private Long tickerId;

    @Schema(description = "Ссылка на страницу оплаты билета")
    private String confirmationUrl;

    @Schema(description = "Сумма платежа в копейках", example = "15000")
    private long amontMinor;

    @Schema(description = "Валюта платежа", example = "RUB")
    private String currency;

}
