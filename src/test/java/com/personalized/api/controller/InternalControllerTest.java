package com.personalized.api.controller;


import com.personalized.api.model.ProductMetadataRequest;
import com.personalized.api.model.ShopperShelfRequest;
import com.personalized.api.service.InternalDataService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InternalControllerTest {

    @Mock
    private InternalDataService internalDataService;

    @InjectMocks
    private InternalController internalController;

    @Test
    void testUpsertProductSuccess() {

        ProductMetadataRequest request = new ProductMetadataRequest();

        doNothing().when(internalDataService).upsertProduct(request);

        ResponseEntity<Void> response =
                internalController.upsertProduct(request);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());

        verify(internalDataService).upsertProduct(request);
        verifyNoMoreInteractions(internalDataService);
    }

    @Test
    void testUpsertProductFailure() {

        ProductMetadataRequest request = new ProductMetadataRequest();

        doThrow(new RuntimeException("Database Error"))
                .when(internalDataService)
                .upsertProduct(request);

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> internalController.upsertProduct(request));

        assertEquals("Database Error", ex.getMessage());

        verify(internalDataService).upsertProduct(request);
    }

    @Test
    void testUpsertShopperShelfSuccess() {

        ShopperShelfRequest request = new ShopperShelfRequest();

        doNothing().when(internalDataService)
                .upsertShopperShelf(request);

        assertDoesNotThrow(() ->
                internalController.upsertShopperShelf(request));

        verify(internalDataService)
                .upsertShopperShelf(request);

        verifyNoMoreInteractions(internalDataService);
    }

    @Test
    void testUpsertShopperShelfFailure() {

        ShopperShelfRequest request = new ShopperShelfRequest();

        doThrow(new IllegalArgumentException("Invalid Shelf"))
                .when(internalDataService)
                .upsertShopperShelf(request);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> internalController.upsertShopperShelf(request));

        assertEquals("Invalid Shelf", ex.getMessage());

        verify(internalDataService)
                .upsertShopperShelf(request);
    }
}