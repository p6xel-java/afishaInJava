package com.example.afisha.dto;

import com.example.afisha.entity.Event;
import com.example.afisha.entity.User;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;


@Entity
@Table(name = "tickets")
@Getter
@Setter
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus status = TicketStatus.PENDING;

    @Column(unique = true)
    private String yookassaPaymentId;

    @Column(unique = true, length = 64)
    private String ticketTokenHash;

    private long amountMinor;

    private String currency = "RUB";

    private Instant createdAt = Instant.now();

    private Instant paidAt;

    private Instant emailSendAt;
}
