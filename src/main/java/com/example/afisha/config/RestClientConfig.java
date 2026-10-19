package com.example.afisha.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient kudaGoClient() {
        return RestClient.builder()
                .baseUrl("https://kudago.com/public-api/v1.4")
                .build();
    }
}
