package com.personalized.api.controller;

import com.personalized.api.model.ProductResponse;
import com.personalized.api.service.ExternalQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
@Tag(name = "External (eCommerce)", description = "Read endpoints for eCommerce servers. Requires EXTERNAL role JWT.")
@SecurityRequirement(name = "bearerAuth")
public class ExternalController {

    private final ExternalQueryService externalQueryService;

    @Operation(
        summary = "Get personalised products for a shopper",
        description = """
            Returns the personalised product shelf for a shopper, sorted by relevancyScore descending.
            Supports optional filtering by category and/or brand.
            Results are cached — first call queries the database, subsequent calls return from cache.
            """,
        parameters = {
            @Parameter(name = "shopperId", description = "Shopper identifier", required = true, example = "S-1000"),
            @Parameter(name = "category",  description = "Filter by product category (optional)", example = "Babies"),
            @Parameter(name = "brand",     description = "Filter by brand (optional)", example = "Babyom"),
            @Parameter(name = "limit",     description = "Max results to return (default: 10, max: 100)", example = "10")
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "Products returned successfully",
                content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "400", description = "shopperId is missing or blank"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT"),
            @ApiResponse(responseCode = "403", description = "Token does not have EXTERNAL role")
        }
    )
    @GetMapping("/products")
    public List<ProductResponse> getProducts(
            @RequestParam @NotBlank(message = "shopperId is required") String shopperId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) Integer limit
    ) {
        return externalQueryService.getProducts(shopperId, category, brand, limit);
    }

}
