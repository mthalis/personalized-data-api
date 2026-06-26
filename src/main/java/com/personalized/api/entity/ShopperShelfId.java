package com.personalized.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@Embeddable
public class ShopperShelfId implements Serializable {

    @Column(name = "shopper_id", length = 64, nullable = false)
    private String shopperId;

    @Column(name = "product_id", length = 64, nullable = false)
    private String productId;

    public ShopperShelfId(String shopperId, String productId) {
        this.shopperId = shopperId;
        this.productId = productId;
    }

}
