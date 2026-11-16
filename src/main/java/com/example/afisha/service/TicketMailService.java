package com.example.afisha.service;

import com.example.afisha.entity.Ticket;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class TicketMailService {

    private final JavaMailSender sender;
    private final TicketQrService qrService;

    @Value("${afisha.app-base-url}")
    private String baseUrl;

    @Value("${afisha.mail.from}")
    private String mailFrom;

    public void sendTicket(Ticket ticket, String token) {
        String url = baseUrl + "/ticket.html?token=" + token;

        try {
            MimeMessage message = sender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());

            helper.setTo(ticket.getUser().getEmail());
            helper.setFrom(mailFrom);
            helper.setSubject("Ваш электронный билет");

            String html = """
                <h2>Оплата подтверждена!</h2>
                <p>Ваш электронный билет готов.</p>
                <p>Название мероприятия: %s</p>
                <p><a href="%s">Открыть билет</a></p>
                <p><img src="cid:ticketQr" alt="QR-код"></p>
                """.formatted(
                    escapeHtml(ticket.getEvent().getTitle()),
                    url);
            helper.setText(html, true);

            helper.addInline("ticketQr", new ByteArrayResource(qrService.generateQrCode(url)), "image/png");
            sender.send(message);
        } catch (Exception e) {
            throw new IllegalStateException("Не удалось отпарвить билет", e);
        }
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }

        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
