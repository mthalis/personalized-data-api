package com.personalized.api.controller;

import com.personalized.api.model.AuthRequest;
import com.personalized.api.service.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthController authController;

    @Test
    void testLoginSuccess() {

        AuthRequest request = new AuthRequest();
        request.setUsername("admin");
        request.setPassword("password");

        when(jwtService.generateToken("admin"))
                .thenReturn("jwt-token");

        Map<String, String> response = authController.login(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.get("token"));
        assertEquals(1, response.size());

        verify(authenticationManager).authenticate(
                any(UsernamePasswordAuthenticationToken.class));

        verify(jwtService).generateToken("admin");
    }

    @Test
    void testLoginAuthenticationFailure() {

        AuthRequest request = new AuthRequest();
        request.setUsername("admin");
        request.setPassword("wrong");

        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        assertThrows(BadCredentialsException.class,
                () -> authController.login(request));

        verify(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        verify(jwtService, never()).generateToken(anyString());
    }
}