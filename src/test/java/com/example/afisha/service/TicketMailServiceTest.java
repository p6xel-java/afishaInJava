package com.example.afisha.service;

import com.example.afisha.entity.Event;
import com.example.afisha.entity.Ticket;
import com.example.afisha.entity.User;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketMailServiceTest {

    @Mock
    private JavaMailSender sender;
    @Mock
    private TicketQrService qr;
    @InjectMocks
    private TicketMailService service;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(service, "baseUrl", "https://afisha.example.test");
        ReflectionTestUtils.setField(service, "mailFrom", "tickets@example.test");
    }

    private Ticket ticket() {
        User user = new User();
        user.setEmail("customer@example.test");
        Event event = new Event();
        event.setTitle("Concert <script>alert('x')</script> & Friends");
        Ticket ticket = new Ticket();
        ticket.setUser(user);
        ticket.setEvent(event);
        return ticket;
    }

    @Test
    void sendsMimeMessageContainingEscapedTitleAndInlineQr() throws Exception {
        MimeMessage message = new MimeMessage(Session.getInstance(new Properties()));
        when(sender.createMimeMessage()).thenReturn(message);
        when(qr.generateQrCode("https://afisha.example.test/ticket.html?token=abc"))
                .thenReturn(new byte[]{1,2,3});
        service.sendTicket(ticket(), "abc");
        verify(sender).send(same(message));
        assertEquals("customer@example.test", message.getAllRecipients()[0].toString());
        assertEquals("tickets@example.test", message.getFrom()[0].toString());
        assertEquals("Ваш электронный билет", message.getSubject());
    }

    private static String findHtml(Multipart multipart) throws Exception {
        for (int i = 0; i < multipart.getCount(); i++) {
            Object content = multipart.getBodyPart(i).getContent();
            if (content instanceof String text && multipart.getBodyPart(i).isMimeType("text/html")) {
                return text;
            }
            if (content instanceof Multipart nested) {
                String text = findHtml(nested);
                if (!text.isEmpty()) return text;
            }
        }
        return "";
    }

    @Test void wrapsMailTransportFailure() {
        when(sender.createMimeMessage()).thenThrow(new IllegalStateException("SMTP down"));
        var error = assertThrows(IllegalStateException.class, () -> service.sendTicket(ticket(), "abc"));
        assertNotNull(error.getCause());
        verify(sender, never()).send(any(MimeMessage.class));
    }

}