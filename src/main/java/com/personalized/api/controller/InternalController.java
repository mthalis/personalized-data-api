package com.personalized.api.controller;

import com.personalized.api.model.ProductMetadataRequest;
import com.personalized.api.model.ShopperShelfRequest;
import com.personalized.api.service.InternalDataService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalController {

    private final InternalDataService internalDataService;

    /**
     * Receive a shopper's full personalised shelf from the data team.
     * Replaces the entire existing shelf for that shopper atomically.
     */
    @PostMapping("/shopper/shelf")
    public ResponseEntity<Void> upsertShopperShelf(@Valid @RequestBody ShopperShelfRequest request) {
        internalDataService.upsertShopperShelf(request);
        return ResponseEntity.ok().build();
    }

    /**
     * Receive product metadata from the data team.
     * Creates or updates a single product record.
     */
    @PostMapping("/product")
    public ResponseEntity<Void> upsertProduct(@Valid @RequestBody ProductMetadataRequest request) {
        internalDataService.upsertProduct(request);
        return ResponseEntity.ok().build();
    }

}
