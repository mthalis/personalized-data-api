package com.personalized.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ShopperShelfRequest {

    @NotBlank(message = "shopperId is required")
    @JsonProperty("shopperId")
    private String shopperId;

    @NotEmpty(message = "shelf must not be empty")
    @Valid
    @JsonProperty("shelf")
    private List<ShelfItem> shelf;

    @Data
    public static class ShelfItem {

        @NotBlank(message = "productId is required")
        @JsonProperty("productId")
        private String productId;

        @NotNull(message = "relevancyScore is required")
        @JsonProperty("relevancyScore")
        private Double relevancyScore;
    }
}