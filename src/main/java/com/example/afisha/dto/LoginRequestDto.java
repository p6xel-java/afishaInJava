package com.example.afisha.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Данные для авторизации")
public class LoginRequestDto {

    @NotBlank(message = "Email обязателен")
    @Email(message = "Некорректный формат email")
    @Schema(description = "Email пользователь", example = "example@gmail.com")
    private String email;


    @NotBlank(message = "Пароль обязателен")
    @Schema(description = "Пароль пользователя", example = "password")
    private String password;

}
