package com.personalized.api.controller;

import com.personalized.api.model.ProductResponse;
import com.personalized.api.service.ExternalQueryService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@Validated
@RequiredArgsConstructor
public class ExternalController {

    private final ExternalQueryService externalQueryService;

    @GetMapping("/products")
    public List<ProductResponse> getProduct(
            @RequestParam @NotBlank(message = "shopperId is required") String shopperId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) Integer limit
    ) {
        return externalQueryService.getProducts(shopperId, category, brand, limit);
    }

}
