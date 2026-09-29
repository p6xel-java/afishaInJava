package com.example.afisha.service;

import com.example.afisha.dto.RegisterRequestDto;
import com.example.afisha.dto.UserResponseDto;

public interface UserService {

    UserResponseDto register(RegisterRequestDto requestDto);


}
