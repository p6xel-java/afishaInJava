package com.example.afisha.config;

import com.example.afisha.security.JwtFilter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @Mock
    private  JwtFilter jwtFilter;

    @InjectMocks
    private SecurityConfig securityConfig;

    @Test
    void passwordEncoderUsesBcryptTest() {
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        assertInstanceOf(PasswordEncoder.class, passwordEncoder);
        String hash = passwordEncoder.encode("secret");
        assertNotEquals("secret", hash);
        assertTrue(passwordEncoder.matches("secret", hash));
        assertFalse(passwordEncoder.matches("present", hash));
    }

    @Test
    void authenticationManagerDelegatesToConfiguration() throws Exception{
        AuthenticationConfiguration authConfig = mock(AuthenticationConfiguration.class);
        AuthenticationManager expected = mock(AuthenticationManager.class);
        when(authConfig.getAuthenticationManager()).thenReturn(expected);
        assertSame(expected, securityConfig.authenticationManager(authConfig));
    }
}