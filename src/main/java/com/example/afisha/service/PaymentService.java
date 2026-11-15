package com.example.afisha.service;

import com.example.afisha.dto.PaymentInitResponse;
import com.example.afisha.dto.Ticket;
import com.example.afisha.dto.TicketStatus;
import com.example.afisha.entity.Event;
import com.example.afisha.entity.User;
import com.example.afisha.repository.EventRepository;
import com.example.afisha.repository.TicketRepository;
import com.example.afisha.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final EventRepository events;
    private final UserRepository users;
    private final TicketRepository tickets;
    private final TicketTokenService tokens;
    private final TicketMailService mailer;
    private final RestClient.Builder clientBuilder;

    @Value("${yookassa.shop-id}")
    private String shopId;

    @Value("${yookassa.secret-key}")
    private String secretKey;

    @Value("${afisha.app-base-url}")
    private String baseUrl;

    private RestClient api() {
        return clientBuilder.baseUrl("https://api.yookassa.ru/v3")
                .defaultHeaders(h -> h.setBasicAuth(shopId, secretKey))
                .build();
    }

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не авторизован");
        }
        return authentication.getName();
    }


    @Transactional
    public PaymentInitResponse create(Long eventId) {

        String email = getCurrentUserEmail();

        User user = users.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не найден"));

        Event event = events.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "Мероприятие не найдено"));

        BigDecimal price = event.getPrice();

        if (price == null || price.signum() <= 0 || price.scale() > 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Некорректная стоимость мероприятия");
        }

        if (event.getStartDateTime() == null || !event.getStartDateTime().isAfter(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Мероприятие уже прошло");
        }

        long amountMinor;

        try {
            amountMinor = price.movePointRight(2).longValueExact();

        } catch (ArithmeticException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Некорректная сумма платежа");
        }

        Ticket ticket = new Ticket();

        ticket.setUser(user);
        ticket.setEvent(event);
        ticket.setStatus(TicketStatus.PENDING);
        ticket.setAmountMinor(amountMinor);
        ticket.setCurrency("RUB");

        ticket = tickets.saveAndFlush(ticket);

        Map<String, Object> request = Map.of(
                "amount", Map.of(
                        "value", price.setScale(2).toPlainString(),
                        "currency", "RUB"
                ),
                "capture", true,
                "confirmation", Map.of(
                        "type", "redirect",
                        "return_url",
                        baseUrl + "/payment-result.html"
                ),
                "description",
                "Билет на мероприятие #" + eventId,
                "metadata", Map.of(
                        "ticketId", ticket.getId().toString()
                )
        );

        JsonNode response = api().post()
                .uri("/payments")
                .header(
                        "Idempotence-Key",
                        "afisha-ticket-" + ticket.getId()
                )
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(JsonNode.class);

        if (response == null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "ЮKassa не вернула ответ");
        }

        String paymentId = response.path("id").asText("");

        String confirmationUrl = response.path("confirmation")
                .path("confirmation_url")
                .asText("");

        if (paymentId.isBlank() || !confirmationUrl.startsWith("https://")) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Некорректный ответ ЮKassa");
        }

        ticket.setYookassaPaymentId(paymentId);
        tickets.save(ticket);

        return new PaymentInitResponse(ticket.getId(), confirmationUrl, amountMinor, "RUB");
    }


    @Transactional
    public void finalizeSuccessfulPayment(String paymentId) {

        JsonNode payment = api().get()
                .uri("/payments/{id}", paymentId)
                .retrieve()
                .body(JsonNode.class);

        if (payment == null) {
            throw new IllegalStateException("ЮKassa не вернула информацию о платеже");
        }

        if (!paymentId.equals(payment.path("id").asText())
                || !"succeeded".equals(payment.path("status").asText())
                || !payment.path("paid").asBoolean()) {
            return;
        }

        String ticketIdText = payment
                .path("metadata")
                .path("ticketId")
                .asText("");

        Long ticketId = Long.valueOf(ticketIdText);

        Ticket ticket = tickets.findWithLockById(ticketId)
                .orElseThrow(() -> new IllegalStateException("Билет не найден"));

        if (!paymentId.equals(ticket.getYookassaPaymentId())) {
            throw new IllegalStateException("Платёж не соответствует билету");
        }

        BigDecimal actualAmount = new BigDecimal(
                payment.path("amount")
                        .path("value")
                        .asText()
        );

        BigDecimal expectedAmount = BigDecimal.valueOf(ticket.getAmountMinor(), 2);

        if (actualAmount.compareTo(expectedAmount) != 0
                || !"RUB".equals(
                payment.path("amount")
                        .path("currency")
                        .asText())) {

            throw new IllegalStateException("Сумма платежа не совпадает");
        }

        if (ticket.getStatus() == TicketStatus.PAID) {
            return;
        }

        ticket.setStatus(TicketStatus.PAID);
        ticket.setPaidAt(Instant.now());

        String token = tokens.newToken();
        ticket.setTicketTokenHash(tokens.hash(token));

        tickets.saveAndFlush(ticket);

        mailer.sendTicket(ticket, token);

        ticket.setEmailSendAt(Instant.now());
    }

}
