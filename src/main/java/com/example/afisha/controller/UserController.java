package com.example.afisha.controller;


import com.example.afisha.dto.UserResponseDto;
import com.example.afisha.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Пользователь", description = "Информация о пользователе")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Получение профиля пользователя", description = "Выводит информацию профиля авторизированного пользователя", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Профиль пользователя успешно получен",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDto.class))),
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
            @ApiResponse(responseCode = "403", description = "Доступ запрещён правилами безопасности",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "Ошибка 403", value = """
                {
                  "timestamp": "2026-11-16T19:30:00",
                  "status": 403,
                  "error": "Forbidden",
                  "message": "Доступ запрещён"
                }
                """))),
            @ApiResponse(responseCode = "500", description = "Ошибка сервера, в том числе если профиль пользователя не найден и исключение не обработано",
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
    public UserResponseDto getMyProfile(@AuthenticationPrincipal UserDetails userDetails) {
        return userService.getCurrentUser(userDetails.getUsername());
    }
}
