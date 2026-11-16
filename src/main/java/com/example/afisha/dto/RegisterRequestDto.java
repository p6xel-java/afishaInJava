package com.example.afisha.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Данные для регистрации пользователя")
public class RegisterRequestDto {

    @NotBlank(message = "Email обязателен")
    @Email(message = "Некорректный формат email")
    @Schema(description = "Email пользователя", example = "example@gmail.com")
    private String email;


    @NotBlank(message = "Пароль обязателен")
    @Size(min = 8, max = 128, message = "Пароль должен быть от 8 до 128 символов")
    @Schema(description = "Пароль пользователя", example = "password")
    private String password;


    @NotBlank(message = "Имя обязательно")
    @Size(min = 2, max = 100)
    @Schema(description = "Имя пользователя", example = "Михаил")
    private String firstName;


    @NotBlank(message = "Фамилия обязательно")
    @Size(min = 2, max = 100)
    @Schema(description = "Фамилия пользователя", example = "Жученко")
    private String lastName;

}
