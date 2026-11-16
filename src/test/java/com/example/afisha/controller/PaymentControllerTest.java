package com.example.afisha.controller;

import com.example.afisha.dto.PaymentInitRequest;
import com.example.afisha.dto.PaymentInitResponse;
import com.example.afisha.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {
    @Mock
    private PaymentService paymentService;
    @InjectMocks
    private PaymentController controller;
    private final ObjectMapper json = new ObjectMapper();

    @Test
    void createDelegateEventId() {
        PaymentInitRequest request = new PaymentInitRequest(42L);
        PaymentInitResponse response = new PaymentInitResponse(7L, "https://example.test/pay", 10000L, "RUB");
        when(paymentService.create(42L)).thenReturn(response);
        assertSame(response, controller.create(request));
        verify(paymentService).create(42L);
    }

    @Test
    void webhookFinalizesSuccessfulNotification() {
        JsonNode notification = json.readTree("""
                {"type":"notification","event":"payment.succeeded","object":{"id":"payment-123456"}}
                """);
        assertEquals(HttpStatus.OK, controller.webhook(notification).getStatusCode());
        verify(paymentService).finalizeSuccessfulPayment("payment-123456");
    }

    @Test
    void unrelatedWebhookIsIgnored() {
        JsonNode notification = json.readTree("""
                {"type":"notification","event":"payment.canceled","object":{"id":"payment-123456"}}
                """);
        assertEquals(HttpStatus.OK, controller.webhook(notification).getStatusCode());
        verifyNoInteractions(paymentService);
    }

    @Test
    void invalidPaymentIdRejected() {
        JsonNode notification = json.readTree("""
                {"type":"notification","event":"payment.succeeded","object":{"id":"bad!"}}
                """);
        assertEquals(HttpStatus.BAD_REQUEST, controller.webhook(notification).getStatusCode());
        verifyNoInteractions(paymentService);
    }

    @Test
    void missingPaymentIdRejected() {
        JsonNode notification = json.readTree("""
                {"type":"notification","event":"payment.succeeded","object":{}}
                """);
        assertEquals(HttpStatus.BAD_REQUEST, controller.webhook(notification).getStatusCode());
        verifyNoInteractions(paymentService);
    }
}