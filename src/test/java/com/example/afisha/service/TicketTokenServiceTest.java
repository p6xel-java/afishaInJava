package com.example.afisha.service;

import org.junit.jupiter.api.Test;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HashSet;
import java.util.HexFormat;
import static org.junit.jupiter.api.Assertions.*;

class TicketTokenServiceTest {
    private final TicketTokenService service = new TicketTokenService();

    @Test
    void tokenContains32RandomBytesAndIsUrlSafe() {
        String token = service.newToken();
        assertTrue(token.matches("[A-Za-z0-9_-]{43}"));
        assertEquals(32, Base64.getUrlDecoder().decode(token).length);
    }

    @Test
    void generatedTokensAreDistinct() {
        HashSet<String> tokens = new HashSet<String>();
        for (int i = 0; i < 100; i++) {
            assertTrue(tokens.add(service.newToken()));
        }
    }

    @Test
    void hashesWithSha256AsLowercaseHex() throws Exception {
        String expected = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest("example".getBytes(StandardCharsets.UTF_8)));
        assertEquals(expected, service.hash("example"));
        assertEquals(64, service.hash("example").length());
        assertNotEquals(service.hash("example"), service.hash("other"));
    }
}
