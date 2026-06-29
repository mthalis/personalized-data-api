package com.personalized.api.config;

import com.personalized.api.security.JwtAuthenticationEntryPoint;
import com.personalized.api.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @Mock
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Mock
    private AuthenticationConfiguration authenticationConfiguration;

    @Mock
    private AuthenticationManager authenticationManager;

    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() throws Exception {
        securityConfig = new SecurityConfig(jwtAuthenticationFilter, jwtAuthenticationEntryPoint);

        setField("username", "testuser");
        setField("password", "testpassword");
    }

    @Test
    void passwordEncoder_ShouldReturnBCryptPasswordEncoder() {
        PasswordEncoder encoder = securityConfig.passwordEncoder();

        assertNotNull(encoder);
        assertTrue(encoder.matches("password", encoder.encode("password")));
    }

    @Test
    void userDetailsService_ShouldCreateInMemoryUser() {
        PasswordEncoder encoder = securityConfig.passwordEncoder();

        UserDetailsService service = securityConfig.userDetailsService(encoder);

        assertNotNull(service);

        UserDetails user = service.loadUserByUsername("testuser");

        assertEquals("testuser", user.getUsername());
        assertTrue(encoder.matches("testpassword", user.getPassword()));
        assertTrue(user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void authenticationManager_ShouldReturnAuthenticationManager() throws Exception {
        when(authenticationConfiguration.getAuthenticationManager())
                .thenReturn(authenticationManager);

        AuthenticationManager result =
                securityConfig.authenticationManager(authenticationConfiguration);

        assertSame(authenticationManager, result);

        verify(authenticationConfiguration).getAuthenticationManager();
    }

    @Test
    void securityFilterChain_ShouldBuildSuccessfully() throws Exception {
        HttpSecurity http = mock(HttpSecurity.class, RETURNS_DEEP_STUBS);

        SecurityFilterChain filterChain = securityConfig.securityFilterChain(http);

        assertNotNull(filterChain);

        verify(http).csrf(any());
        verify(http).build();
    }

    private void setField(String fieldName, Object value) throws Exception {
        Field field = SecurityConfig.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(securityConfig, value);
    }
}
