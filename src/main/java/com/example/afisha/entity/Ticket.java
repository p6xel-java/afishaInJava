package com.example.afisha.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;


@Entity
@Table(name = "tickets")
@Getter
@Setter
@Schema(description = "Билет, приобретенный пользователем на мероприятие")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID билета в базе данных", example = "12")
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @Schema(description = "Пользователь, который приобрел билет")
    private User user;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    @Schema(description = "Мероприятие, на которое покупают билет")
    private Event event;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Schema(description = "Статус покупки билета", example = "PENDING, PAID")
    private TicketStatus status = TicketStatus.PENDING;


    @Column(unique = true)
    @Schema(description = "ID платежа в системе Yookassa")
    private String yookassaPaymentId;


    @Column(unique = true, length = 64)
    @Schema(description = "Hash код билета")
    private String ticketTokenHash;

    @Schema(description = "Сумма платежа в копейках", example = "15000")
    private long amountMinor;


    @Schema(description = "Валюта платежа", example = "RUB")
    private String currency = "RUB";


    @Schema(description = "Время создания билета", example = "2026-11-15T18:24:49.161943Z")
    private Instant createdAt = Instant.now();

    @Schema(description = "Время покупки билета", example = "2026-11-15T18:24:51.161943Z")
    private Instant paidAt;

    @Schema(description = "Время отправки билета на почту", example = "2026-11-15T18:24:51.161943Z")
    private Instant emailSendAt;
}
