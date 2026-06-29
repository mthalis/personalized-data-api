package com.personalized.api.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler handler;

    @Mock
    private MethodArgumentNotValidException methodArgumentNotValidException;

    @Mock
    private BindingResult bindingResult;

    // -----------------------------
    // 1. MethodArgumentNotValidException
    // -----------------------------
    @Test
    void testHandleValidation() {

        FieldError error1 = new FieldError("obj", "productId", "must not be blank");
        FieldError error2 = new FieldError("obj", "name", "must not be blank");

        when(methodArgumentNotValidException.getBindingResult())
                .thenReturn(bindingResult);

        when(bindingResult.getFieldErrors())
                .thenReturn(List.of(error1, error2));

        ProblemDetail result = handler.handleValidation(methodArgumentNotValidException);

        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getStatus());
        assertEquals("productId: must not be blank, name: must not be blank", result.getDetail());
    }

    // -----------------------------
    // 2. ConstraintViolationException
    // -----------------------------
    @Test
    void testHandleConstraint() {

        ConstraintViolationException ex =
                new ConstraintViolationException("shopperId is required", null);

        ProblemDetail result = handler.handleConstraint(ex);

        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getStatus());
        assertEquals("shopperId is required", result.getDetail());
    }

    // -----------------------------
    // 3. EntityNotFoundException
    // -----------------------------
    @Test
    void testHandleEntityNotFound() {

        EntityNotFoundException ex =
                new EntityNotFoundException("Product not found");

        ProblemDetail result = handler.handleEntityNotFound(ex);

        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getStatus());
        assertEquals("Product not found", result.getDetail());
    }

    // -----------------------------
    // 4. MissingServletRequestParameterException
    // -----------------------------
    @Test
    void testMissingRequestParameter() {

        MissingServletRequestParameterException ex =
                new MissingServletRequestParameterException("shopperId", "String");

        ProblemDetail result = handler.missingRequestParameter(ex);

        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getStatus());
        assertTrue(result.getDetail().contains("shopperId"));
    }

    // -----------------------------
    // 5. Generic Exception (catch-all)
    // -----------------------------
    @Test
    void testHandleGeneric() {

        Exception ex = new RuntimeException("Something went wrong");

        ProblemDetail result = handler.handleGeneric(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), result.getStatus());
        assertEquals("An unexpected error occurred.", result.getDetail());
    }
}