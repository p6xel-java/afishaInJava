package com.example.afisha.controller;

import com.example.afisha.dto.JwtResponseDto;
import com.example.afisha.dto.LoginRequestDto;
import com.example.afisha.dto.RegisterRequestDto;
import com.example.afisha.dto.UserResponseDto;
import com.example.afisha.entity.Role;
import com.example.afisha.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthController authController;

    @Test
    void registerDeligatesToUserServiceTest() {
        RegisterRequestDto requestDto = new RegisterRequestDto("example@mail.ru", "qwerty", "Mike", "Putin");
        UserResponseDto expectDto = new UserResponseDto(3L, "example@mail.ru", "Mike", "Putin", Role.ROLE_USER);

        when(userService.register(requestDto)).thenReturn(expectDto);
        assertSame(expectDto, authController.register(requestDto));
        verify(userService).register(requestDto);
    }

    @Test
    void loginDeligatesToUserServiceTest() {
        LoginRequestDto requestDto = new LoginRequestDto();
        requestDto.setEmail("example@mail.ru");
        requestDto.setPassword("password");
        JwtResponseDto expect = new JwtResponseDto("token");
        when(userService.login(requestDto)).thenReturn(expect);

        assertSame(expect, authController.login(requestDto));
        verify(userService).login(requestDto);
    }

}