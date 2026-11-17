package com.example.afisha.service;


import com.example.afisha.dto.kudago.KudaGoEventDto;
import com.example.afisha.dto.kudago.KudaGoResponceDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Service
public class KudaGoEventFetcherServiceImpl implements EventFetcherService{

    @Value("${kudago.api.url}")
    private String KUDAGO_API_URL;

    private final RestTemplate restTemplate;

    public KudaGoEventFetcherServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public List<KudaGoEventDto> fetchEventsFromExternalApi() {
        KudaGoResponceDto response = restTemplate.getForObject(KUDAGO_API_URL, KudaGoResponceDto.class);

        if (response != null && response.getResults() != null) {
            return response.getResults();
        }

        return Collections.emptyList();
    }
}
