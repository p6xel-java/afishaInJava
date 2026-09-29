package com.example.afisha.controller;

import com.example.afisha.dto.RegisterRequestDto;
import com.example.afisha.dto.UserResponseDto;
import com.example.afisha.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public UserResponseDto register(@RequestBody RegisterRequestDto registerDto) {
        return userService.register(registerDto);
    }

}
