package com.example.afisha.service;

import com.example.afisha.dto.kudago.KudaGoEventDto;
import com.example.afisha.dto.kudago.KudaGoResponceDto;
import com.example.afisha.entity.Event;
import com.example.afisha.repository.EventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KudaGoServiceImplTest {

    @Mock
    private RestClient restClient;

    @Mock
    private EventRepository repository;

    @Mock
    private RestClient.RequestHeadersUriSpec requestUri;

    @Mock
    private RestClient.RequestHeadersSpec requestHeaders;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @InjectMocks
    private KudaGoServiceImpl service;

    private void stubGetRequest(KudaGoResponceDto... responses) {
        when(restClient.get()).thenReturn(requestUri);
        when(requestUri.uri(any(Function.class))).thenReturn(requestHeaders);
        when(requestHeaders.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(KudaGoResponceDto.class)).thenReturn(responses[0], java.util.Arrays.copyOfRange(responses, 1, responses.length));
    }

    @Test
    void importsNewEventAndParsesSimplePriceAndStartDate() {
        KudaGoEventDto dto = externalEvent(123L, "Concert", "100000");
        KudaGoEventDto.DateTime date = new KudaGoEventDto.DateTime();
        date.setStart(1_700_000_000L);
        dto.setDates(List.of(date));
        stubGetRequest(response(List.of(dto)));
        when(repository.findByExternalId("123")).thenReturn(Optional.empty());

        int imported = service.importEventsFromKudaGo("spb", 1);

        assertEquals(1, imported);
        ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
        verify(repository).save(captor.capture());
        Event event = captor.getValue();
        assertEquals("123", event.getExternalId());
        assertEquals("Concert", event.getTitle());
        assertEquals("Description", event.getDescription());
        assertEquals("KudaGo Event", event.getVenue());
        assertEquals(new BigDecimal("100000"), event.getPrice());
        assertEquals(LocalDateTime.ofInstant(Instant.ofEpochSecond(1_700_000_000L),
                ZoneId.systemDefault()), event.getStartDateTime());
    }

    @Test
    void updatesExistingEventPreservingInternalId() {
        KudaGoEventDto dto = externalEvent(999L, "Updated", "Бесплатно");
        stubGetRequest(response(List.of(dto)));
        Event existing = new Event();
        existing.setId(10L);
        existing.setExternalId("999");
        when(repository.findByExternalId("999")).thenReturn(Optional.of(existing));

        assertEquals(1, service.importEventsFromKudaGo("spb", 1));
        ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
        verify(repository).save(captor.capture());
        assertSame(existing, captor.getValue());
        assertEquals(10L, existing.getId());
        assertEquals("Updated", existing.getTitle());
        assertEquals(BigDecimal.ZERO, existing.getPrice());
    }

    @Test
    void importsAcrossTwoPages() {
        stubGetRequest(response(List.of(externalEvent(1L, "A", "150"))),
                response(List.of(externalEvent(2L, "B", "250"))));
        when(repository.findByExternalId(anyString())).thenReturn(Optional.empty());

        assertEquals(2, service.importEventsFromKudaGo("spb", 2));
        verify(repository, times(2)).save(any(Event.class));
        verify(restClient, times(2)).get();
    }

    @Test
    void nullResponseStopsImport() {
        when(restClient.get()).thenReturn(requestUri);
        when(requestUri.uri(any(Function.class))).thenReturn(requestHeaders);
        when(requestHeaders.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(KudaGoResponceDto.class)).thenReturn(null);

        assertEquals(0, service.importEventsFromKudaGo("spb", 3));
        verify(restClient).get();
        verify(repository, never()).save(any());
    }

    @Test
    void nullResultsStopsImport() {
        stubGetRequest(response(null));
        assertEquals(0, service.importEventsFromKudaGo("spb", 2));
        verify(repository, never()).save(any());
    }

    @Test
    void zeroPageCountDoesNotRequestExternalApi() {
        assertEquals(0, service.importEventsFromKudaGo("spb", 0));
        verifyNoInteractions(restClient, repository);
    }

    private static KudaGoEventDto externalEvent(long id, String title, String price) {
        KudaGoEventDto dto = new KudaGoEventDto();
        dto.setId(id);
        dto.setTitle(title);
        dto.setDescription("Description");
        dto.setPrice(price);
        return dto;
    }

    private static KudaGoResponceDto response(List<KudaGoEventDto> results) {
        KudaGoResponceDto response = new KudaGoResponceDto();
        response.setResults(results);
        return response;
    }

}