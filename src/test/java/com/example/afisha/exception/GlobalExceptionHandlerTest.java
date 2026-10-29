package com.example.afisha.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    @Test
    void dublicateUserProducesConflictResponse() {
        UserAlreadyExistsException exception = new UserAlreadyExistsException("Email already exists");
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        ResponseEntity<Map<String, Object>> response = handler.handleUserAlreadyExists(exception);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().get("status"));
        assertEquals("Conflict", response.getBody().get("error"));
        assertEquals("Email already exists", response.getBody().get("message"));
        assertNotNull(response.getBody().get("timestamp"));
    }

}