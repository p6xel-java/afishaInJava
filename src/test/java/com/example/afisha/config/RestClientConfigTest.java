package com.example.afisha.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.*;

class RestClientConfigTest {

    @Test
    void createRestClientConfigTest() {
        RestClient restClient = new RestClientConfig().kudaGoClient();
        assertNotNull(restClient);
    }

}