package com.personalized.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "shopper_shelf_items")
@Getter
@Setter
@NoArgsConstructor
public class ShopperShelf {

    @EmbeddedId
    private ShopperShelfId id;

    @Column(name = "relevancy_score", nullable = false)
    private Double relevancyScore;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("productId")
    @JoinColumn(
            name = "product_id",
            referencedColumnName = "product_id"
    )
    private Product product;
}
