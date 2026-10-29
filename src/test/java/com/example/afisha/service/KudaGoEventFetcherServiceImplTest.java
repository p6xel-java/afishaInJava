package com.example.afisha.service;

import com.example.afisha.dto.kudago.KudaGoEventDto;
import com.example.afisha.dto.kudago.KudaGoResponceDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KudaGoEventFetcherServiceImplTest {
    private static final String URL =
            "https://kudago.com/public-api/v1.4/events/?location=spb&fields=id,title,description";

    @Mock RestTemplate restTemplate;
    @InjectMocks KudaGoEventFetcherServiceImpl service;

    @Test
    void returnsEventsFromExternalResponse() {
        KudaGoEventDto event = new KudaGoEventDto();
        event.setId(1L);
        KudaGoResponceDto response = new KudaGoResponceDto();
        response.setResults(List.of(event));
        when(restTemplate.getForObject(URL, KudaGoResponceDto.class)).thenReturn(response);

        List<KudaGoEventDto> result = service.fetchEventsFromExternalApi();

        assertEquals(1, result.size());
        assertSame(event, result.get(0));
        verify(restTemplate).getForObject(URL, KudaGoResponceDto.class);
    }

    @Test
    void returnsEmptyListWhenResponseIsNull() {
        when(restTemplate.getForObject(URL, KudaGoResponceDto.class)).thenReturn(null);
        assertTrue(service.fetchEventsFromExternalApi().isEmpty());
    }

    @Test
    void returnsEmptyListWhenResultsAreNull() {
        when(restTemplate.getForObject(URL, KudaGoResponceDto.class)).thenReturn(new KudaGoResponceDto());
        assertTrue(service.fetchEventsFromExternalApi().isEmpty());
    }
}
