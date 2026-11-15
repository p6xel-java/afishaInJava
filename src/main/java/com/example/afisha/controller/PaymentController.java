package com.example.afisha.controller;

import com.example.afisha.dto.PaymentInitRequest;
import com.example.afisha.dto.PaymentInitResponse;
import com.example.afisha.service.PaymentService;
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
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/intent")
    @PreAuthorize("isAuthenticated()")
    public PaymentInitResponse create(@RequestBody @Valid PaymentInitRequest request) {
        return paymentService.create(request.getEventId());
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(@RequestBody JsonNode notification) {

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
