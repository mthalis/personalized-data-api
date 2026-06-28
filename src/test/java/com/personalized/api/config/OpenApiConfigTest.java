package com.personalized.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class OpenApiConfigTest {

    @Test
    void testOpenAPIConfiguration() {

        OpenApiConfig config = new OpenApiConfig();

        OpenAPI openAPI = config.openAPI();

        assertNotNull(openAPI);

        // Info
        assertEquals("Personalized Data API",
                openAPI.getInfo().getTitle());

        assertEquals("1.0.0",
                openAPI.getInfo().getVersion());

        assertEquals("Personalized Data API Team",
                openAPI.getInfo().getContact().getName());

        // Servers
        assertNotNull(openAPI.getServers());
        assertEquals(2, openAPI.getServers().size());

        assertEquals("http://localhost:8080",
                openAPI.getServers().get(0).getUrl());

        assertEquals("https://api.proddomain.com",
                openAPI.getServers().get(1).getUrl());

        // Security Scheme
        assertNotNull(openAPI.getComponents());
        assertNotNull(openAPI.getComponents().getSecuritySchemes());

        SecurityScheme scheme =
                openAPI.getComponents()
                        .getSecuritySchemes()
                        .get("bearerAuth");

        assertNotNull(scheme);

        assertEquals(SecurityScheme.Type.HTTP, scheme.getType());
        assertEquals("bearer", scheme.getScheme());
        assertEquals("JWT", scheme.getBearerFormat());

        // Security Requirement
        assertNotNull(openAPI.getSecurity());
        assertEquals(1, openAPI.getSecurity().size());
        assertTrue(openAPI.getSecurity().get(0).containsKey("bearerAuth"));
    }
}