
package com.example.afisha.config;

import com.example.afisha.repository.EventRepository;
import com.example.afisha.service.KudaGoService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventDataInitializer implements ApplicationRunner {

    private final EventRepository eventRepository;
    private final KudaGoService kudaGoService;

    @Value("${afisha.import.location:spb}")
    private String location;

    @Value("${afisha.import.pages:5}")
    private int pages;

    @Override
    public void run(ApplicationArguments args) {

        long eventCount = eventRepository.count();

        log.info("Количество мероприятий в БД: {}", eventCount);

        if (eventCount > 0) {
            log.info("База уже содержит мероприятия. Импорт пропущен.");
            return;
        }

        log.info("Начинаем импорт мероприятий из KudaGo");

        try {
            int importedCount =
                    kudaGoService.importEventsFromKudaGo(
                            location, pages
                    );

            log.info(
                    "Импорт завершён. Загружено мероприятий: {}",
                    importedCount
            );

        } catch (Exception e) {
            log.error(
                    "Ошибка автоматического импорта мероприятий из KudaGo",
                    e
            );
        }
    }
}
