package com.example.afisha.controller;

import com.example.afisha.dto.JwtResponseDto;
import com.example.afisha.dto.LoginRequestDto;
import com.example.afisha.dto.RegisterRequestDto;
import com.example.afisha.dto.UserResponseDto;
import com.example.afisha.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Авторизация", description = "Регистрация и авторизация пользователя")
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    @Operation(summary = "Регистрация пользователя", description = "Регистрация нового пользователя для покупки билетов")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Пользователь успешно зарегистрирован",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Некорректные данные регистрации",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "Ошибка 400", value = """
                {
                  "timestamp": "2026-11-16T19:30:00",
                  "status": 400,
                  "error": "Bad Request",
                  "message": "Ошибка валидации",
                  "fieldErrors": {
                    "email": "Некорректный формат email"
                  }
                }
                """))),
            @ApiResponse(responseCode = "409", description = "Пользователь с таким email уже зарегистрирован",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "Ошибка 409", value = """
                {
                  "timestamp": "2026-11-16T19:30:00",
                  "status": 409,
                  "error": "Conflict",
                  "message": "Пользователь с таким email уже зарегистрирован"
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
    public UserResponseDto register(@RequestBody  @Valid RegisterRequestDto registerDto) {
        return userService.register(registerDto);
    }




    @PostMapping("/login")
    @Operation(summary = "Авторизация пользователя", description = "Авторизация уже существующего пользователя для работы с сайтом")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Авторизация выполнена успешно",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = JwtResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Некорректные данные запроса",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "Ошибка 400", value = """
                {
                  "timestamp": "2026-11-16T19:30:00",
                  "status": 400,
                  "error": "Bad Request",
                  "message": "Ошибка валидации",
                  "fieldErrors": {
                    "email": "Некорректный формат email"
                  }
                }
                """))),
            @ApiResponse(responseCode = "401", description = "Неверный email или пароль (отказ аутентификации)",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "Ошибка 401", value = """
                {
                  "timestamp": "2026-11-16T19:30:00",
                  "status": 401,
                  "error": "Unauthorized",
                  "message": "Требуется авторизация"
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
    public JwtResponseDto login(@RequestBody @Valid LoginRequestDto loginDto) {
        return userService.login(loginDto);
    }

}
