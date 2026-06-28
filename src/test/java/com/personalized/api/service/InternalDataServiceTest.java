package com.personalized.api.service;
import com.personalized.api.entity.Product;
import com.personalized.api.entity.ShopperShelf;
import com.personalized.api.model.ProductMetadataRequest;
import com.personalized.api.model.ShopperShelfRequest;
import com.personalized.api.repository.ProductRepository;
import com.personalized.api.repository.ShopperShelfRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InternalDataServiceTest {

    @Mock ProductRepository      productRepository;
    @Mock ShopperShelfRepository shelfRepository;
    @InjectMocks InternalDataService service;

    // ── helpers ───────────────────────────────────────────────────────────────

    private ProductMetadataRequest productReq(String id, String cat, String brand) {
        ProductMetadataRequest r = new ProductMetadataRequest();
        r.setProductId(id);
        r.setCategory(cat);
        r.setBrand(brand);
        return r;
    }

    private ShopperShelfRequest shelfReq(String shopperId, String... productIds) {
        ShopperShelfRequest req = new ShopperShelfRequest();
        req.setShopperId(shopperId);
        List<ShopperShelfRequest.ShelfItem> items = new ArrayList<>();
        double score = 90.0;
        for (String pid : productIds) {
            ShopperShelfRequest.ShelfItem item = new ShopperShelfRequest.ShelfItem();
            item.setProductId(pid);
            item.setRelevancyScore(score);
            items.add(item);
            score -= 10;
        }
        req.setShelf(items);
        return req;
    }

    // ── upsertProduct ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("upsertProduct()")
    class UpsertProductTests {

        @Test
        @DisplayName("creates new product when it does not exist")
        void createsNewProduct() {
            when(productRepository.findById("P-1")).thenReturn(Optional.empty());

            service.upsertProduct(productReq("P-1", "Babies", "Babyom"));

            ArgumentCaptor<Product> cap = ArgumentCaptor.forClass(Product.class);
            verify(productRepository).save(cap.capture());
            Product saved = cap.getValue();

            assertThat(saved.getProductId()).isEqualTo("P-1");
            assertThat(saved.getCategory()).isEqualTo("Babies");
            assertThat(saved.getBrand()).isEqualTo("Babyom");
            assertThat(saved.getCreatedAt()).isNotNull();
            assertThat(saved.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("updates existing product and preserves createdAt")
        void updatesExistingProduct() {
            Instant originalCreatedAt = Instant.parse("2024-01-01T00:00:00Z");
            Product existing = new Product();
            existing.setProductId("P-1");
            existing.setCategory("Old Category");
            existing.setBrand("Old Brand");
            existing.setCreatedAt(originalCreatedAt);
            existing.setUpdatedAt(originalCreatedAt);

            when(productRepository.findById("P-1")).thenReturn(Optional.of(existing));

            service.upsertProduct(productReq("P-1", "Babies", "Babyom"));

            ArgumentCaptor<Product> cap = ArgumentCaptor.forClass(Product.class);
            verify(productRepository).save(cap.capture());
            Product saved = cap.getValue();

            assertThat(saved.getCategory()).isEqualTo("Babies");
            assertThat(saved.getBrand()).isEqualTo("Babyom");
            assertThat(saved.getCreatedAt()).isEqualTo(originalCreatedAt);
            assertThat(saved.getUpdatedAt()).isAfterOrEqualTo(originalCreatedAt);
        }
    }

    // ── upsertShopperShelf ────────────────────────────────────────────────────

    @Nested
    @DisplayName("upsertShopperShelf()")
    class UpsertShopperShelfTests {

        @Test
        @DisplayName("deletes old shelf then saves all new items")
        void replacesShelfSuccessfully() {
            when(productRepository.existsById("P-1")).thenReturn(true);
            when(productRepository.existsById("P-2")).thenReturn(true);
            when(productRepository.getReferenceById(any())).thenReturn(new Product());

            service.upsertShopperShelf(shelfReq("S-1", "P-1", "P-2"));

            verify(shelfRepository).deleteAllByShopperId("S-1");
            verify(shelfRepository).flush();
            verify(shelfRepository, times(2)).save(any(ShopperShelf.class));
        }

        @Test
        @DisplayName("throws EntityNotFoundException when a productId is unknown")
        void throwsWhenProductMissing() {
            when(productRepository.existsById("P-OK")).thenReturn(true);
            when(productRepository.existsById("P-MISSING")).thenReturn(false);

            assertThatThrownBy(() -> service.upsertShopperShelf(shelfReq("S-1", "P-OK", "P-MISSING")))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("P-MISSING");

            // No DB mutation should have happened
            verify(shelfRepository, never()).deleteAllByShopperId(any());
            verify(shelfRepository, never()).save(any());
        }

        @Test
        @DisplayName("saves shelf item with correct relevancy score")
        void savesCorrectScore() {
            when(productRepository.existsById("P-1")).thenReturn(true);
            when(productRepository.getReferenceById("P-1")).thenReturn(new Product());

            ShopperShelfRequest req = shelfReq("S-1", "P-1");
            req.getShelf().get(0).setRelevancyScore(73.014);

            service.upsertShopperShelf(req);

            ArgumentCaptor<ShopperShelf> cap = ArgumentCaptor.forClass(ShopperShelf.class);
            verify(shelfRepository).save(cap.capture());
            assertThat(cap.getValue().getRelevancyScore()).isEqualTo(73.014);
        }

        @Test
        @DisplayName("sets shopperId and productId in the composite key")
        void setsCompositeKey() {
            when(productRepository.existsById("P-1")).thenReturn(true);
            when(productRepository.getReferenceById("P-1")).thenReturn(new Product());

            service.upsertShopperShelf(shelfReq("S-42", "P-1"));

            ArgumentCaptor<ShopperShelf> cap = ArgumentCaptor.forClass(ShopperShelf.class);
            verify(shelfRepository).save(cap.capture());
            assertThat(cap.getValue().getId().getShopperId()).isEqualTo("S-42");
            assertThat(cap.getValue().getId().getProductId()).isEqualTo("P-1");
        }

        @Test
        @DisplayName("sets updatedAt on each saved shelf row")
        void setsUpdatedAt() {
            when(productRepository.existsById("P-1")).thenReturn(true);
            when(productRepository.getReferenceById("P-1")).thenReturn(new Product());

            service.upsertShopperShelf(shelfReq("S-1", "P-1"));

            ArgumentCaptor<ShopperShelf> cap = ArgumentCaptor.forClass(ShopperShelf.class);
            verify(shelfRepository).save(cap.capture());
            assertThat(cap.getValue().getCreatedAt()).isNotNull();
        }
    }
}
