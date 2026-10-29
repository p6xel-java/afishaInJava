package com.example.afisha.security;

import com.example.afisha.entity.Role;
import com.example.afisha.entity.User;
import com.example.afisha.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService service;

    @Test
    void loadsExistingUser() {
        User user = new User(3L, "example@mail.ru", "password", "Mike", "Putin", Role.ROLE_ADMIN);
        when(userRepository.findByEmail("example@mail.ru")).thenReturn(Optional.of(user));
        UserDetails details = service.loadUserByUsername("example@mail.ru");
        assertEquals("example@mail.ru", details.getUsername());
        assertEquals("password", details.getPassword());
    }

    @Test
    void rejectsUnknownEmail() {
        when(userRepository.findByEmail("unknown@mail.ru")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("unknown@mail.ru"));
    }

}