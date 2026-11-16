package com.example.afisha.service;

import com.example.afisha.entity.Event;
import com.example.afisha.entity.Ticket;
import com.example.afisha.entity.TicketStatus;
import com.example.afisha.entity.User;
import com.example.afisha.repository.EventRepository;
import com.example.afisha.repository.TicketRepository;
import com.example.afisha.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private EventRepository events;
    @Mock
    private UserRepository users;
    @Mock
    private TicketRepository tickets;
    @Mock
    private TicketTokenService tokens;
    @Mock
    private TicketMailService mailer;
    private PaymentService service;
    private MockRestServiceServer server;


    @BeforeEach
    void setup() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        service = new PaymentService(events, users, tickets, tokens, mailer, builder);
        ReflectionTestUtils.setField(service, "shopId", "test-shop");
        ReflectionTestUtils.setField(service, "secretKey", "test-secret");
        ReflectionTestUtils.setField(service, "baseUrl", "https://afisha.example.test");
    }

    @AfterEach
    void cleanup() { SecurityContextHolder.clearContext(); }

    private void authenticate() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("buyer@example.test", null, List.of()));
    }

    private Event validEvent() {
        Event event = new Event(); event.setId(9L);
        event.setPrice(new BigDecimal("1250.50"));
        event.setStartDateTime(LocalDateTime.now().plusDays(10));
        return event;
    }

    private void stubUserAndEvent(Event event) {
        authenticate();
        when(users.findByEmail("buyer@example.test")).thenReturn(Optional.of(new User()));
        when(events.findById(9L)).thenReturn(Optional.of(event));
    }

    @Test
    void anonymousUserCannotCreatePayment() {
        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> service.create(9L));
        assertEquals(HttpStatus.UNAUTHORIZED, error.getStatusCode());
        verifyNoInteractions(events, users, tickets);
    }

    @Test
    void missingEventReturns404() {
        authenticate();
        when(users.findByEmail("buyer@example.test")).thenReturn(Optional.of(new User()));
        when(events.findById(9L)).thenReturn(Optional.empty());
        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> service.create(9L));
        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
        verifyNoInteractions(tickets);
    }

    @Test
    void rejectsInvalidPricesAndExpiredEvent() {
        Event event = validEvent();
        stubUserAndEvent(event);
        for (BigDecimal price : new BigDecimal[]{null, BigDecimal.ZERO, new BigDecimal("-1"), new BigDecimal("1.001")}) {
            event.setPrice(price);
            assertEquals(HttpStatus.BAD_REQUEST,
                    assertThrows(ResponseStatusException.class, () -> service.create(9L)).getStatusCode());
        }
        event.setPrice(new BigDecimal("100"));
        event.setStartDateTime(LocalDateTime.now().minusMinutes(1));
        assertEquals(HttpStatus.BAD_REQUEST,
                assertThrows(ResponseStatusException.class, () -> service.create(9L)).getStatusCode());
        verifyNoInteractions(tickets);
    }

    @Test
    void createsPendingTicketAndReturnsConfirmation() {
        stubUserAndEvent(validEvent());
        when(tickets.saveAndFlush(any(Ticket.class))).thenAnswer(invocation -> {
            Ticket t = invocation.getArgument(0); t.setId(15L); return t;
        });
        server.expect(requestTo("https://api.yookassa.ru/v3/payments"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Idempotence-Key", "afisha-ticket-15"))
                .andExpect(jsonPath("$.amount.value").value("1250.50"))
                .andExpect(jsonPath("$.metadata.ticketId").value("15"))
                .andRespond(withSuccess("""
                        {"id":"payment-12345","confirmation":{"confirmation_url":"https://pay.example.test/checkout"}}
                        """, MediaType.APPLICATION_JSON));
        var result = service.create(9L);
        assertEquals(15L, result.getTickerId());
        assertEquals(125050L, result.getAmontMinor());
        assertEquals("RUB", result.getCurrency());
        assertEquals("https://pay.example.test/checkout", result.getConfirmationUrl());
        verify(tickets).save(argThat(t -> t.getStatus() == TicketStatus.PENDING &&
                t.getAmountMinor() == 125050L && "payment-12345".equals(t.getYookassaPaymentId())));
        server.verify();
    }

    @Test
    void rejectsInvalidPaymentGatewayResponse() {
        stubUserAndEvent(validEvent());
        when(tickets.saveAndFlush(any(Ticket.class))).thenAnswer(invocation -> {
            Ticket t = invocation.getArgument(0); t.setId(15L); return t;
        });
        server.expect(requestTo("https://api.yookassa.ru/v3/payments"))
                .andRespond(withSuccess("""
                        {"id":"payment-12345","confirmation":{"confirmation_url":"http://insecure.example.test"}}
                        """, MediaType.APPLICATION_JSON));
        assertEquals(HttpStatus.BAD_GATEWAY,
                assertThrows(ResponseStatusException.class, () -> service.create(9L)).getStatusCode());
        verify(tickets, never()).save(any(Ticket.class));
        server.verify();
    }

    private void paymentResponse(String paymentId, String json) {
        server.expect(requestTo("https://api.yookassa.ru/v3/payments/" + paymentId))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));
    }

    private String paymentJson(String amount) {
        return """
                {"id":"payment-12345","status":"succeeded","paid":true,
                 "metadata":{"ticketId":"15"},"amount":{"value":"%s","currency":"RUB"}}
                """.formatted(amount);
    }

    private Ticket savedTicket() {
        Ticket ticket = new Ticket(); ticket.setId(15L);
        ticket.setYookassaPaymentId("payment-12345");
        ticket.setAmountMinor(125050L);
        ticket.setStatus(TicketStatus.PENDING);
        return ticket;
    }

    @Test
    void successfulPaymentMarksTicketPaidAndSendsEmail() {
        Ticket ticket = savedTicket();
        paymentResponse("payment-12345", paymentJson("1250.50"));
        when(tickets.findWithLockById(15L)).thenReturn(Optional.of(ticket));
        when(tokens.newToken()).thenReturn("ticket-token");
        when(tokens.hash("ticket-token")).thenReturn("hashed-token");
        service.finalizeSuccessfulPayment("payment-12345");
        assertEquals(TicketStatus.PAID, ticket.getStatus());
        assertNotNull(ticket.getPaidAt());
        assertNotNull(ticket.getEmailSendAt());
        assertEquals("hashed-token", ticket.getTicketTokenHash());
        verify(tickets).saveAndFlush(same(ticket));
        verify(mailer).sendTicket(ticket, "ticket-token");
        server.verify();
    }

    @Test
    void duplicateSuccessfulWebhookDoesNotSendTwice() {
        Ticket ticket = savedTicket(); ticket.setStatus(TicketStatus.PAID);
        paymentResponse("payment-12345", paymentJson("1250.50"));
        when(tickets.findWithLockById(15L)).thenReturn(Optional.of(ticket));
        service.finalizeSuccessfulPayment("payment-12345");
        verifyNoInteractions(mailer, tokens);
        verify(tickets, never()).saveAndFlush(any(Ticket.class));
        server.verify();
    }

    @Test
    void mismatchedAmountCannotIssueTicket() {
        paymentResponse("payment-12345", paymentJson("1.00"));
        when(tickets.findWithLockById(15L)).thenReturn(Optional.of(savedTicket()));
        assertThrows(IllegalStateException.class, () -> service.finalizeSuccessfulPayment("payment-12345"));
        verifyNoInteractions(mailer, tokens);
        server.verify();
    }

    @Test
    void unconfirmedPaymentIsIgnored() {
        paymentResponse("payment-12345", """
                {"id":"payment-12345","status":"pending","paid":false}
                """);
        service.finalizeSuccessfulPayment("payment-12345");
        verifyNoInteractions(tickets, mailer, tokens);
        server.verify();
    }

    @Test
    void wrongPaymentIdOnTicketCannotIssueTicket() {
        Ticket ticket = savedTicket(); ticket.setYookassaPaymentId("different-payment");
        paymentResponse("payment-12345", paymentJson("1250.50"));
        when(tickets.findWithLockById(15L)).thenReturn(Optional.of(ticket));
        assertThrows(IllegalStateException.class, () -> service.finalizeSuccessfulPayment("payment-12345"));
        verifyNoInteractions(mailer, tokens);
        server.verify();
    }

}