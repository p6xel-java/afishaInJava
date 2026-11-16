package com.example.afisha.controller;

import com.example.afisha.dto.PaymentInitRequest;
import com.example.afisha.dto.PaymentInitResponse;
import com.example.afisha.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Покупка билетов")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/intent")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Создание платежа на покупку билета", description = "Инициализирует платёж за билет на выбранное мероприятие и возвращает информацию о созданном платеже, включая ссылку для оплаты", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Платёж успешно инициализирован",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PaymentInitResponse.class))),
            @ApiResponse(responseCode = "400", description = "Неверные данные запроса, стоимость мероприятия, сумма платежа или мероприятие уже прошло",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "Ошибка 400", value = """
                {
                  "timestamp": "2026-11-16T19:30:00",
                  "status": 400,
                  "error": "Bad Request",
                  "message": "Некорректные параметры запроса"
                }
                """))),
            @ApiResponse(responseCode = "401", description = "Требуется авторизация",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "Ошибка 401", value = """
                {
                  "timestamp": "2026-11-16T19:30:00",
                  "status": 401,
                  "error": "Unauthorized",
                  "message": "Требуется авторизация"
                }
                """))),
            @ApiResponse(responseCode = "404", description = "Мероприятие с указанным ID не найдено",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "Ошибка 404", value = """
                {
                  "timestamp": "2026-11-16T19:30:00",
                  "status": 404,
                  "error": "Not Found",
                  "message": "Ресурс не найден"
                }
                """))),
            @ApiResponse(responseCode = "502", description = "ЮKassa вернула пустой или некорректный ответ",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "Ошибка 502", value = """
                {
                  "timestamp": "2026-11-16T19:30:00",
                  "status": 502,
                  "error": "Bad Gateway",
                  "message": "Некорректный ответ ЮKassa"
                }
                """))),
            @ApiResponse(responseCode = "500", description = "Непредвиденная ошибка сервера или взаимодействия с платёжной системой",
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
    public PaymentInitResponse create(@RequestBody @Valid PaymentInitRequest request) {
        return paymentService.create(request.getEventId());
    }

    @PostMapping("/webhook")
    @Operation(summary = "Обработка уведомлений от Юкассы", description = "Принимает webhook уведомление от Юкассы об изменении статуса платежа и обрабатыет их")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Уведомление обработано или проигнорировано"),
            @ApiResponse(responseCode = "400", description = "Некорректный JSON (JSON-ошибка в теле ответа) или неверный ID платежа (пустое тело)",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "Некорректный JSON", value = """
                {
                  "timestamp": "2026-11-16T19:30:00",
                  "status": 400,
                  "error": "Bad Request",
                  "message": "Некорректный JSON или формат значения"
                }
                """))),
            @ApiResponse(responseCode = "500", description = "Ошибка проверки платежа, поиска билета, суммы или отправки письма",
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
    public ResponseEntity<Void> webhook(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Webhook-уведомление от ЮKassa",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "Успешная оплата",
                            summary = "Уведомление об успешной оплате билета",
                            value = """
                    {
                      "type": "notification",
                      "event": "payment.succeeded",
                      "object": {
                        "id": "328d7a4d-000f-5001-8000-1ea7e1cc3ae0",
                        "status": "succeeded",
                        "amount": {
                          "value": "1500.00",
                          "currency": "RUB"
                        },
                        "paid": true,
                        "metadata": {
                          "eventId": "2"
                        },
                        "test": true
                      }
                    }
                    """
                    )
            )) @RequestBody JsonNode notification) {

        String type = notification.path("type").asText();
        String event = notification.path("event").asText();

        if (!"notification".equals(type) || !"payment.succeeded".equals(event)) {
            return ResponseEntity.ok().build();
        }

        String paymentId = notification.path("object").path("id").asString("");

        if (!paymentId.matches("[A-Za-z0-9-]{10,80}")) {
            return ResponseEntity.badRequest().build();
        }

        paymentService.finalizeSuccessfulPayment(paymentId);
        return ResponseEntity.ok().build();
    }



}
