package com.example.afisha.controller;

import com.example.afisha.dto.UserResponseDto;
import com.example.afisha.entity.Role;
import com.example.afisha.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private UserController userController;

    @Test
    void myProfileUsesEmailOfAuthenticatedPrincipal() {
        when(userDetails.getUsername()).thenReturn("example@mail.ru");
        UserResponseDto expect = new UserResponseDto(1L, "example@mail.ru", "Mike", "Putin", Role.ROLE_USER);
        when(userService.getCurrentUser("example@mail.ru")).thenReturn(expect);
        assertSame(expect, userController.getMyProfile(userDetails));
        verify(userService).getCurrentUser("example@mail.ru");
    }

}