package com.example.afisha.controller;

import com.example.afisha.dto.EventRequestDto;
import com.example.afisha.dto.EventResponseDto;
import com.example.afisha.service.EventService;
import com.example.afisha.service.KudaGoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor

@Tag(name = "Мероприятия", description = "Управление мероприятиями")
public class EventController {

    private final EventService eventService;

    private final KudaGoService kudaGoService;


    @PostMapping("/sync")
    @Operation(hidden = true)
    public String syncEvents() {
        eventService.syncEvents();
        return "sync completed";
    }

    @GetMapping
    @Operation(summary = "Получение всех мероприятий", description = "Возвращает подробную информацию о всех мероприятиях в базе данных")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список мероприятий успешно получен"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "Ошибка 500", value = """
                {
                  "timestamp": "2026-11-16T19:30:00",
                  "status": 500,
                  "error": "Internal Server Error",
                  "message": "Внутренняя ошибка сервера"
                }
                """)))
    })
    public List<EventResponseDto> getAllEvents() {
        return eventService.getAllEvents();
    }


    @GetMapping("/{id}")
    @Operation(summary = "Получения мероприятия по ID", description = "Возвращает подробную информацию о конкретном мероприятии в базе данных")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Мероприятие успешно найдено",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = EventResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Идентификатор должен быть положительным целым числом",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "Ошибка 400", value = """
                {
                  "timestamp": "2026-11-16T19:30:00",
                  "status": 400,
                  "error": "Bad Request",
                  "message": "Некорректные параметры запроса"
                }
                """))),
            @ApiResponse(responseCode = "404", description = "Мероприятие с указанным ID не найдено",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "Ошибка 404", value = """
                {
                  "timestamp": "2026-11-16T19:30:00",
                  "status": 404,
                  "error": "Not Found",
                  "message": "Ресурс не найден"
                }
                """))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "Ошибка 500", value = """
                {
                  "timestamp": "2026-11-16T19:30:00",
                  "status": 500,
                  "error": "Internal Server Error",
                  "message": "Внутренняя ошибка сервера"
                }
                """)))
    })
    public EventResponseDto getEventById(@Parameter(description = "ID мероприятия") @PathVariable("id") @Min(1) Long id) {
        return eventService.getEventById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Создание мероприятия", description = "Создание нового мероприятия", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Мероприятие успешно создано",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = EventResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Некорректные данные мероприятия",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "Ошибка 400", value = """
                {
                  "timestamp": "2026-11-16T19:30:00",
                  "status": 400,
                  "error": "Bad Request",
                  "message": "Некорректные параметры запроса"
                }
                """))),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "Ошибка 401", value = """
                {
                  "timestamp": "2026-11-16T19:30:00",
                  "status": 401,
                  "error": "Unauthorized",
                  "message": "Требуется авторизация"
                }
                """))),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав: требуется роль ADMIN",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "Ошибка 403", value = """
                {
                  "timestamp": "2026-11-16T19:30:00",
                  "status": 403,
                  "error": "Forbidden",
                  "message": "Доступ запрещён"
                }
                """))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "Ошибка 500", value = """
                {
                  "timestamp": "2026-11-16T19:30:00",
                  "status": 500,
                  "error": "Internal Server Error",
                  "message": "Внутренняя ошибка сервера"
                }
                """)))
    })
    public EventResponseDto createEvent(@RequestBody @Valid EventRequestDto requestDto) {
        return eventService.createEvent(requestDto);
    }

    @PostMapping("/import")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(hidden = true)
    public ResponseEntity<Map<String, Object>> importEvents(
            @RequestParam(defaultValue = "spb") @NotBlank String location,
            @RequestParam(defaultValue = "1") @Min(1) @Max(100) int pages) {
        int count = kudaGoService.importEventsFromKudaGo(location, pages);
        return ResponseEntity.ok(Map.of(
                "message", "Импорт из KudeGo завершен успешно", "importedCount", count
        ));
    }


}
