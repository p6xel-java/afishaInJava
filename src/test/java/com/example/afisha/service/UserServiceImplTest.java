package com.example.afisha.service;

import com.example.afisha.dto.*;
import com.example.afisha.entity.Role;
import com.example.afisha.entity.User;
import com.example.afisha.exception.UserAlreadyExistsException;
import com.example.afisha.repository.UserRepository;
import com.example.afisha.security.JwtUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock AuthenticationManager authenticationManager;
    @Mock JwtUtils jwtUtils;
    @InjectMocks UserServiceImpl service;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void registerCreatesUserWithEncodedPasswordAndDefaultRole() {
        RegisterRequestDto request = new RegisterRequestDto("example@mail.ru", "password", "Mike", "Putin");
        when(userRepository.existsByEmail("example@mail.ru")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(7L);
            return user;
        });

        UserResponseDto result = service.register(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertAll(
                () -> assertEquals("example@mail.ru", saved.getEmail()),
                () -> assertEquals("hashed", saved.getPassword()),
                () -> assertEquals("Mike", saved.getFirstName()),
                () -> assertEquals("Putin", saved.getLastName()),
                () -> assertEquals(Role.ROLE_USER, saved.getRole()),
                () -> assertEquals(7L, result.getId()),
                () -> assertEquals(Role.ROLE_USER, result.getRole())
        );
        verify(passwordEncoder).encode("password");
    }

    @Test
    void registerRejectsDuplicateEmailWithoutSaving() {
        RegisterRequestDto request = new RegisterRequestDto("example@mail.ru", "password", "Mike", "Putin");
        when(userRepository.existsByEmail("example@mail.ru")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> service.register(request));

        verify(userRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void loginAuthenticatesSetsContextAndReturnsJwt() {
        LoginRequestDto request = new LoginRequestDto();
        request.setEmail("example@mail.ru");
        request.setPassword("password");
        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(jwtUtils.generateToken("example@mail.ru")).thenReturn("jwt-token");

        JwtResponseDto result = service.login(request);

        ArgumentCaptor<UsernamePasswordAuthenticationToken> captor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(captor.capture());
        assertEquals("example@mail.ru", captor.getValue().getPrincipal());
        assertEquals("password", captor.getValue().getCredentials());
        assertSame(auth, SecurityContextHolder.getContext().getAuthentication());
        assertEquals("jwt-token", result.getToken());
    }

    @Test
    void loginPropagatesAuthenticationErrorAndDoesNotCreateJwt() {
        LoginRequestDto request = new LoginRequestDto();
        request.setEmail("example@mail.ru");
        request.setPassword("incorrect");
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("wrong password"));

        assertThrows(BadCredentialsException.class, () -> service.login(request));
        verifyNoInteractions(jwtUtils);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void getCurrentUserReturnsMappedProfile() {
        User user = new User(9L, "example@mail.ru", "password", "Mike", "Putin", Role.ROLE_ADMIN);
        when(userRepository.findByEmail("example@mail.ru")).thenReturn(Optional.of(user));

        UserResponseDto profile = service.getCurrentUser("example@mail.ru");

        assertAll(
                () -> assertEquals(9L, profile.getId()),
                () -> assertEquals("example@mail.ru", profile.getEmail()),
                () -> assertEquals("Mike", profile.getFirstName()),
                () -> assertEquals("Putin", profile.getLastName()),
                () -> assertEquals(Role.ROLE_ADMIN, profile.getRole())
        );
    }

    @Test
    void getCurrentUserThrowsForUnknownEmail() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> service.getCurrentUser("missing@example.com"));
    }
}
