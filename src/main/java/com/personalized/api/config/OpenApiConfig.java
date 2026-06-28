package com.personalized.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger / OpenAPI configuration.
 *
 * Access UI at: http://localhost:8080/swagger-ui.html
 *
 * How to test secured endpoints in Swagger UI:
 *   1. Call POST /auth/token with your credentials
 *   2. Copy the accessToken from the response
 *   3. Click "Authorize" button at the top → paste token → Authorize
 *   4. All subsequent requests will include the Bearer token
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER_AUTH = "bearerAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Personalized Data API")
                        .description("""
                                API service providing personalised product shelves to eCommerce servers.
                                
                                **Authentication flow:**
                                1. `POST /auth/token` with your credentials to receive a JWT
                                2. Click **Authorize** and enter: `<your_token>` (without 'Bearer')
                                3. All secured endpoints will automatically use the token
                                
                                **Roles:**
                                - `INTERNAL` — data team: can write products and shopper shelves
                                - `EXTERNAL` — eCommerce servers: can read personalised products
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Personalized Data API Team")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local dev"),
                        new Server().url("https://api.proddomain.com").description("Production")
                ))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH,
                                new SecurityScheme()
                                        .name(BEARER_AUTH)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter the JWT token from POST /auth/token")));
    }
}
