package com.example.afisha.dto;

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
public class RegisterRequestDto {

    @NotBlank(message = "Email обязателен")
    @Email(message = "Некорректный формат email")
    private String email;

    @NotBlank(message = "Пароль обязателен")
    @Size(min = 8, max = 128, message = "Пароль должен быть от 8 до 128 символов")
    private String password;

    @NotBlank(message = "Имя обязательно")
    @Size(min = 2, max = 100)
    private String firstName;

    @NotBlank(message = "Фамилия обязательно")
    @Size(min = 2, max = 100)
    private String lastName;

}
