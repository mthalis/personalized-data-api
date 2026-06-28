package com.personalized.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** POST /auth/token  request body */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequest {

    @NotBlank(message = "username is required")
    @JsonProperty("username")
    private String username;

    @NotBlank(message = "password is required")
    @JsonProperty("password")
    private String password;
}