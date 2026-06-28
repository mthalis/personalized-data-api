package com.personalized.api.controller;

import com.personalized.api.model.ProductResponse;
import com.personalized.api.service.ExternalQueryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExternalControllerTest {

    @Mock
    private ExternalQueryService externalQueryService;

    @InjectMocks
    private ExternalController externalController;

    @Test
    void testGetProducts() {

        ProductResponse product1 = new ProductResponse();
        ProductResponse product2 = new ProductResponse();

        List<ProductResponse> expected =
                List.of(product1, product2);

        when(externalQueryService.getProducts(
                "S-1000",
                "Babies",
                "Babyom",
                10))
                .thenReturn(expected);

        List<ProductResponse> actual = externalController.getProducts(
                "S-1000",
                "Babies",
                "Babyom",
                10);

        assertNotNull(actual);
        assertEquals(2, actual.size());
        assertSame(expected, actual);

        verify(externalQueryService, times(1))
                .getProducts(
                        "S-1000",
                        "Babies",
                        "Babyom",
                        10);

        verifyNoMoreInteractions(externalQueryService);
    }

    @Test
    void testGetProducts_ServiceThrowsException() {

        when(externalQueryService.getProducts(
                anyString(),
                any(),
                any(),
                any()))
                .thenThrow(new RuntimeException("Database error"));

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> externalController.getProducts(
                        "S-1000",
                        "Babies",
                        "Babyom",
                        10));

        assertEquals("Database error", ex.getMessage());

        verify(externalQueryService)
                .getProducts("S-1000",
                        "Babies",
                        "Babyom",
                        10);
    }

    @Test
    void testGetProducts_IllegalArgumentException() {

        when(externalQueryService.getProducts(
                anyString(),
                any(),
                any(),
                any()))
                .thenThrow(new IllegalArgumentException("Invalid shopper"));

        assertThrows(
                IllegalArgumentException.class,
                () -> externalController.getProducts(
                        "S-1000",
                        null,
                        null,
                        null));

        verify(externalQueryService)
                .getProducts("S-1000",
                        null,
                        null,
                        null);
    }
}