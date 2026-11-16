package com.example.afisha.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Ответ сервера после успешной авторизации пользователя")
public class JwtResponseDto {

    @Schema(description = "JWT токен доступа для авторизации пользователя")
    private String token;

}
