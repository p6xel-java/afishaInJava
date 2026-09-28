package com.example.afisha.service;


import com.example.afisha.dto.KudaGoEventDto;
import com.example.afisha.dto.KudaGoResponceDto;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Service
public class KudaGoEventFetcherServiceImpl implements EventFetcherService{

    private static final String KUDAGO_API_URL = "https://kudago.com/public-api/v1.4/events/?location=spb&fields=id,title,description";

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
