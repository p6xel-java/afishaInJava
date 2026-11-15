package com.example.afisha.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TicketResponseDto {

    private Long ticketId;

    private String status;

    private Instant paidAt;

    private EventResponseDto event;

}
