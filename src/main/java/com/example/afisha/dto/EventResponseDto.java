package com.example.afisha.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class EventResponseDto {

    private Long id;

    private String title;

    private String description;

    private String venue;

    private BigDecimal price;

    private LocalDateTime startDateTime;

}
