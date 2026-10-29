package com.example.afisha.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilsTest {

    private JwtUtils jwtUtils;
    private static final String TEST_SECRET = "secret3563254237234624624524762642634672354236662536723542636723642364652932";

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", 60000);
    }

    @Test
    void generatedTokenIsValidAndContainsEmail() {
        String token = jwtUtils.generateToken("example@mail.ru");
        assertNotNull(token);
        assertTrue(jwtUtils.validateToken(token));
        assertEquals("example@mail.ru", jwtUtils.getEmailFromToken(token));
    }

    @Test
    void invalidFormatFailsValidation() {
        assertFalse(jwtUtils.validateToken("sfsgs"));
    }

    @Test
    void expiredTokenFailsValidation() {
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", -60000);
        String token = jwtUtils.generateToken("example@mail.ru");
        assertFalse(jwtUtils.validateToken(token));
    }
}