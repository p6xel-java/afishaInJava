package com.example.afisha.dto;

import com.example.afisha.entity.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Объект пользователя")
public class UserResponseDto {

    @Schema(description = "ID пользователя в базе данных", example = "2")
    private Long id;


    @Schema(description = "Email пользователя", example = "example@gmail.com")
    private String email;


    @Schema(description = "Имя пользователя", example = "Михаил")
    private String firstName;


    @Schema(description = "Фамилия пользователя", example = "Жученко")
    private String lastName;


    @Schema(description = "Роль пользователя", example = "USER, ADMIN")
    private Role role;

}
