package com.personalized.api.service;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();

        String secret = Base64.getEncoder().encodeToString(
                "01234567890123456789012345678901"
                        .getBytes(StandardCharsets.UTF_8));

        ReflectionTestUtils.setField(jwtService, "secret", secret);
        ReflectionTestUtils.setField(jwtService, "expiration", 60_000L);
    }

    @Test
    void shouldGenerateToken() {
        String token = jwtService.generateToken("dinesh");

        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void shouldExtractUsername() {
        String token = jwtService.generateToken("dinesh");

        assertEquals("dinesh", jwtService.extractUsername(token));
    }

    @Test
    void shouldReturnTrueForValidToken() {
        String token = jwtService.generateToken("dinesh");

        assertTrue(jwtService.isValid(token));
    }

    @Test
    void shouldReturnFalseForInvalidToken() {
        assertFalse(jwtService.isValid("invalid.token"));
    }

    @Test
    void shouldThrowExceptionForInvalidToken() {
        assertThrows(JwtException.class,
                () -> jwtService.extractUsername("invalid.token"));
    }
}