package com.personalized.api.repository;

import com.personalized.api.entity.ShopperShelf;
import com.personalized.api.entity.ShopperShelfId;
import com.personalized.api.model.ProductResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShopperShelfRepository extends JpaRepository<ShopperShelf, ShopperShelfId> {

    @Query("""
            SELECT new com.personalized.api.model.ProductResponse(
                p.productId, p.category, p.brand, s.relevancyScore
            )
            FROM ShopperShelf s
            JOIN s.product p
            WHERE s.id.shopperId = :shopperId
              AND (:category IS NULL OR p.category = :category)
              AND (:brand    IS NULL OR p.brand    = :brand)
            ORDER BY s.relevancyScore DESC
            """)
    List<ProductResponse> findPersonalisedProducts(
            @Param("shopperId") String shopperId,
            @Param("category")  String category,
            @Param("brand")     String brand
    );

    /** Remove all shelf rows for a shopper before replacing the full shelf. */
    @Modifying
    @Query("DELETE FROM ShopperShelf s WHERE s.id.shopperId = :shopperId")
    void deleteAllByShopperId(@Param("shopperId") String shopperId);
}
