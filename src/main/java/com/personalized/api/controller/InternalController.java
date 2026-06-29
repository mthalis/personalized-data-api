package com.personalized.api.controller;

import com.personalized.api.model.ProductMetadataRequest;
import com.personalized.api.model.ShopperShelfRequest;
import com.personalized.api.service.InternalDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
@Tag(name = "Internal (Data Team)", description = "Write endpoints for the data team. Requires INTERNAL role JWT.")
@SecurityRequirement(name = "bearerAuth")
public class InternalController {

    private final InternalDataService internalDataService;

    @Operation(
            summary = "Register or update product metadata",
            description = "Creates a new product or updates an existing one. Must be called before uploading a shelf that references this product.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Product saved successfully"),
                    @ApiResponse(responseCode = "400", description = "Validation error — missing or blank fields"),
                    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT"),
                    @ApiResponse(responseCode = "403", description = "Token does not have INTERNAL role")
            }
    )
    @PostMapping("/product")
    public ResponseEntity<Void> upsertProduct(@Valid @RequestBody ProductMetadataRequest request) {
        internalDataService.upsertProduct(request);
        log.info("action=upsert_product_request productId={}", request.getProductId());
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Upload personalised shelf for a shopper",
            description = """
            Atomically replaces the full product shelf for a shopper.
            All productIds in the shelf must already be registered via POST /internal/product.
            Products are returned to eCommerce sorted by relevancyScore descending.
            """,
            responses = {
                    @ApiResponse(responseCode = "204", description = "Shelf saved successfully"),
                    @ApiResponse(responseCode = "400", description = "Validation error"),
                    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT"),
                    @ApiResponse(responseCode = "403", description = "Token does not have INTERNAL role"),
                    @ApiResponse(responseCode = "422", description = "One or more productIds are not registered")
            }
    )
    @PostMapping("/shopper/shelf")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void upsertShopperShelf(@Valid @RequestBody ShopperShelfRequest request) {
        log.info("action=upsert_shelf_request shopperId={} ", request.getShopperId());
        internalDataService.upsertShopperShelf(request);
    }
}
