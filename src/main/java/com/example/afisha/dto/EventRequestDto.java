package com.example.afisha.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class EventRequestDto {

    private String title;

    private String description;

    private String venue;

    private BigDecimal price;

    private LocalDateTime startDateTime;
}
