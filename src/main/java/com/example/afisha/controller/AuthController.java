package com.example.afisha.controller;

import com.example.afisha.dto.JwtResponseDto;
import com.example.afisha.dto.LoginRequestDto;
import com.example.afisha.dto.RegisterRequestDto;
import com.example.afisha.dto.UserResponseDto;
import com.example.afisha.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public UserResponseDto register(@RequestBody RegisterRequestDto registerDto) {
        return userService.register(registerDto);
    }

    @PostMapping("/login")
    public JwtResponseDto login(@RequestBody LoginRequestDto loginDto) {
        return userService.login(loginDto);
    }

}
