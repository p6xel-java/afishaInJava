package com.example.afisha.service;


import com.example.afisha.dto.kudago.KudaGoEventDto;
import com.example.afisha.dto.kudago.KudaGoResponceDto;
import com.example.afisha.entity.Event;
import com.example.afisha.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class KudaGoServiceImpl implements KudaGoService{

    private final RestClient kudaGoRestClient;

    private final EventRepository eventRepository;


    @Override
    public int importEventsFromKudaGo(String location, int pageCount) {

        int importCount = 0;
        for(int page = 1; page <= pageCount; page++) {
            int currentPage = page;
            KudaGoResponceDto responce = kudaGoRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/events/")
                            .queryParam("location", location)
                            .queryParam("page", currentPage)
                            .queryParam("page_size", 20)
                            .queryParam("fields", "id,title,description,price,dates")
                            .build())
                    .retrieve()
                    .body(KudaGoResponceDto.class);
            if(responce == null || responce.getResults() == null) {
                break;
            }

            for(KudaGoEventDto dto: responce.getResults()) {
                if (saveOrUpdateEvent(dto)) {
                    importCount++;
                }
            }
        }
        return importCount;
    }

    private boolean saveOrUpdateEvent(KudaGoEventDto dto) {
        String externalId = String.valueOf(dto.getId());
        Event event = eventRepository.findByExternalId(externalId)
                .orElse(new Event());

        event.setExternalId(externalId);
        event.setTitle(dto.getTitle());
        event.setDescription(dto.getDescription());
        event.setVenue("KudaGo Event");
        event.setPrice(parsePrise(dto.getPrice()));

        if (dto.getDates()!= null && !dto.getDates().isEmpty()) {
            Long startSeconds = dto.getDates().get(0).getStart();
            if (startSeconds != null && startSeconds > 0) {
                event.setStartDateTime(LocalDateTime.ofInstant(Instant.ofEpochSecond(startSeconds), ZoneId.systemDefault()));

            }
        }
        eventRepository.save(event);
        return true;
    }

    private String cleanHtmlTags(String text) {
        if (text == null) {
            return "";
        } else {
            return text.replaceAll("<[^>]*>", "");
        }
    }

    private BigDecimal parsePrise(String priceStr) {
        if (priceStr== null || priceStr.isBlank() || priceStr.toLowerCase(Locale.ROOT).contains("бесплатно")) {
            return BigDecimal.ZERO;
        }
        try {
            String clean = priceStr.replaceAll("[^0-9]", "");
            return clean.isEmpty() ? BigDecimal.ZERO : new BigDecimal(clean);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }
}
