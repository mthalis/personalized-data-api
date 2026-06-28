package com.personalized.api.exception;

import com.personalized.api.controller.ExternalController;
import com.personalized.api.service.ExternalQueryService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ExternalController.class)
class GlobalExceptionHandlerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean
    ExternalQueryService externalQueryService;

    @Test
    @WithMockUser(roles = "EXTERNAL")
    @DisplayName("handles ConstraintViolationException with 400")
    void handlesConstraintViolation() throws Exception {
        when(externalQueryService.getProducts(any(), any(), any(), any()))
                .thenThrow(new ConstraintViolationException("invalid param", Set.of()));

        mockMvc.perform(get("/api/products").param("shopperId", "S-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "EXTERNAL")
    @DisplayName("handles EntityNotFoundException with 422")
    void handlesEntityNotFound() throws Exception {
        when(externalQueryService.getProducts(any(), any(), any(), any()))
                .thenThrow(new EntityNotFoundException("Product not found: P-X"));

        mockMvc.perform(get("/api/products").param("shopperId", "S-1"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value("Product not found: P-X"));
    }

    @Test
    @WithMockUser(roles = "EXTERNAL")
    @DisplayName("handles generic Exception with 500")
    void handlesGenericException() throws Exception {
        when(externalQueryService.getProducts(any(), any(), any(), any()))
                .thenThrow(new RuntimeException("Something blew up"));

        mockMvc.perform(get("/api/products").param("shopperId", "S-1"))
                .andExpect(status().isInternalServerError());
    }
}
