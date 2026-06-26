package com.personalized.api.service;

import com.personalized.api.entity.Product;
import com.personalized.api.entity.ShopperShelf;
import com.personalized.api.entity.ShopperShelfId;
import com.personalized.api.model.ProductMetadataRequest;
import com.personalized.api.model.ShopperShelfRequest;
import com.personalized.api.repository.ProductRepository;
import com.personalized.api.repository.ShopperShelfRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class InternalDataService {

    private final ProductRepository productRepository;
    private final ShopperShelfRepository shelfRepository;

    /**
     * Upsert product metadata — DB-agnostic load-then-save.
     * Works on both H2 (local/test) and PostgreSQL (prod).
     */
    @Transactional
    public void upsertProduct(ProductMetadataRequest req) {
        log.info("Upserting product: {}", req.getProductId());

        Product product = productRepository.findById(req.getProductId())
                .orElseGet(Product::new);

        product.setProductId(req.getProductId());
        product.setCategory(req.getCategory());
        product.setBrand(req.getBrand());
        product.setUpdatedAt(Instant.now());

        if (product.getCreatedAt() == null) {
            product.setCreatedAt(Instant.now());
        }
        productRepository.save(product);
    }

    /**
     * Atomically replace the full shelf for a shopper.
     *
     * Order of operations:
     *   1. Validate all productIds exist — fail fast before any mutation.
     *   2. Delete the current shelf rows.
     *   3. Insert all new shelf rows.
     *   4. Evict cache so the next external read reflects the new data.
     */
    @Transactional
    @CacheEvict(value = "shelf", allEntries = true)
    public void upsertShopperShelf(ShopperShelfRequest req) {
        log.info("Replacing shelf for shopper: {} ({} items)",
                req.getShopperId(), req.getShelf().size());

        // Step 1 — validate all product references before touching the DB
        for (ShopperShelfRequest.ShelfItem item : req.getShelf()) {
            if (!productRepository.existsById(item.getProductId())) {
                throw new EntityNotFoundException(
                        "Product not found: " + item.getProductId()
                                + ". Register it via POST /internal/product first.");
            }
        }

        // Step 2 — delete old shelf
        shelfRepository.deleteAllByShopperId(req.getShopperId());
        shelfRepository.flush();

        // Step 3 — insert new shelf
        for (ShopperShelfRequest.ShelfItem item : req.getShelf()) {
            Product product = productRepository.getReferenceById(item.getProductId());

            ShopperShelf shelf = new ShopperShelf();
            shelf.setId(new ShopperShelfId(req.getShopperId(), item.getProductId()));
            shelf.setProduct(product);
            shelf.setRelevancyScore(item.getRelevancyScore());
            shelf.setCreatedAt(Instant.now());

            shelfRepository.save(shelf);
        }
    }
}
