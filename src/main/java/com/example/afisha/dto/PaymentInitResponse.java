package com.example.afisha.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInitResponse {

    private Long tickerId;

    private String confirmationUrl;

    private long amontMinor;

    private String currency;

}
