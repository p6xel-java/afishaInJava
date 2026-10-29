package com.example.afisha.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtFilterTest {

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain chain;

    @InjectMocks
    private JwtFilter filter;

    @AfterEach
    void cleanContex() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void validBearerTokenSetsAuthenticatedUser() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
        when(jwtUtils.validateToken("valid-token")).thenReturn(true);
        when(jwtUtils.getEmailFromToken("valid-token")).thenReturn("example@mail.ru");
        UserDetails details = User.withUsername("example@mail.ru").password("password").authorities("ROLE_USER").build();
        when(userDetailsService.loadUserByUsername("example@mail.ru")).thenReturn(details);

        filter.doFilterInternal(request, response, chain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals("example@mail.ru", authentication.getName());
        assertTrue(authentication.getAuthorities().stream().anyMatch(authority -> authority.getAuthority().equals("ROLE_USER")));
        verify(chain).doFilter(request, response);
    }

    @Test
    void missingHeaderPassesThroughUnauthenticated() throws Exception{
        filter.doFilterInternal(request, response, chain);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(jwtUtils, userDetailsService);
        verify(chain).doFilter(request, response);
    }

    @Test
    void nonBearerHeaderDoesNotCallJwtUtils() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Basic credentials");
        filter.doFilterInternal(request, response, chain);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(jwtUtils, userDetailsService);
        verify(chain).doFilter(request, response);
    }

    @Test
    void invalidJwtPassesThroughUnaunthenticated() throws Exception{
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid");
        when(jwtUtils.validateToken("invalid")).thenReturn(false);
        filter.doFilterInternal(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(chain).doFilter(request, response);
    }

}