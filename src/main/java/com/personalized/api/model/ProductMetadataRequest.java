package com.personalized.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProductMetadataRequest {

    @NotBlank(message = "productId is required")
    @JsonProperty("productId")
    private String productId;

    @NotBlank(message = "category is required")
    @JsonProperty("category")
    private String category;

    @NotBlank(message = "brand is required")
    @JsonProperty("brand")
    private String brand;
}