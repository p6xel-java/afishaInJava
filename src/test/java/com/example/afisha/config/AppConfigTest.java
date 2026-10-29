package com.example.afisha.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;

class AppConfigTest {

    @Test
    void createRestTempleTest() {
        RestTemplate restTemplate = new RestTemplate();
        assertNotNull(restTemplate);
    }

}