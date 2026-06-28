package com.personalized.api.service;

import com.personalized.api.model.ProductResponse;
import com.personalized.api.repository.ShopperShelfRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExternalQueryServiceTest {

    @Mock  ShopperShelfRepository shelfRepository;
    @InjectMocks ExternalQueryService service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "maxLimit",     100);
        ReflectionTestUtils.setField(service, "defaultLimit", 10);
    }

    private ProductResponse product(String id, double score) {
        return new ProductResponse(id, "Cat", "Brand", score);
    }

    // ── resolveLimit ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("resolveLimit()")
    class ResolveLimitTests {

        @Test void nullReturnsDefault()     { assertThat(service.resolveLimit(null)).isEqualTo(10); }
        @Test void zeroReturnsDefault()     { assertThat(service.resolveLimit(0)).isEqualTo(10); }
        @Test void negativeReturnsDefault() { assertThat(service.resolveLimit(-5)).isEqualTo(10); }
        @Test void withinBound()            { assertThat(service.resolveLimit(50)).isEqualTo(50); }
        @Test void atMax()                  { assertThat(service.resolveLimit(100)).isEqualTo(100); }
        @Test void exceedsMaxClamped()      { assertThat(service.resolveLimit(200)).isEqualTo(100); }
    }

    // ── getProducts ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getProducts()")
    class GetProductsTests {

        @Test
        @DisplayName("returns all results when count is below limit")
        void returnsAll_whenBelowLimit() {
            List<ProductResponse> rows = List.of(product("A", 90), product("B", 50));
            when(shelfRepository.findPersonalisedProducts("S-1", null, null)).thenReturn(rows);

            List<ProductResponse> result = service.getProducts("S-1", null, null, 10);

            assertThat(result).hasSize(2);
            verify(shelfRepository).findPersonalisedProducts("S-1", null, null);
        }

        @Test
        @DisplayName("slices results when count exceeds requested limit")
        void slicesResults_whenAboveLimit() {
            List<ProductResponse> rows = List.of(
                    product("A", 90), product("B", 70), product("C", 50));
            when(shelfRepository.findPersonalisedProducts("S-1", null, null)).thenReturn(rows);

            List<ProductResponse> result = service.getProducts("S-1", null, null, 2);

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getProductId()).isEqualTo("A");
        }

        @Test
        @DisplayName("passes category filter through to repository")
        void passesCategoryFilter() {
            when(shelfRepository.findPersonalisedProducts("S-1", "Babies", null))
                    .thenReturn(List.of(product("BB-1", 80)));

            List<ProductResponse> result = service.getProducts("S-1", "Babies", null, null);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getProductId()).isEqualTo("BB-1");
        }

        @Test
        @DisplayName("passes brand filter through to repository")
        void passesBrandFilter() {
            when(shelfRepository.findPersonalisedProducts("S-1", null, "Babyom"))
                    .thenReturn(List.of(product("BB-1", 80)));

            List<ProductResponse> result = service.getProducts("S-1", null, "Babyom", null);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("passes both filters together")
        void passesBothFilters() {
            when(shelfRepository.findPersonalisedProducts("S-1", "Babies", "Babyom"))
                    .thenReturn(List.of(product("BB-1", 80)));

            List<ProductResponse> result = service.getProducts("S-1", "Babies", "Babyom", 5);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("returns empty list when shopper has no shelf")
        void returnsEmpty_whenNoShelf() {
            when(shelfRepository.findPersonalisedProducts("S-UNKNOWN", null, null))
                    .thenReturn(Collections.emptyList());

            List<ProductResponse> result = service.getProducts("S-UNKNOWN", null, null, null);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("over-limit request is clamped to maxLimit before querying")
        void clampsOverLimitRequest() {
            when(shelfRepository.findPersonalisedProducts("S-1", null, null))
                    .thenReturn(List.of(product("A", 1)));

            service.getProducts("S-1", null, null, 999);

            verify(shelfRepository).findPersonalisedProducts("S-1", null, null);
        }

        @Test
        @DisplayName("uses default limit when null limit passed")
        void usesDefaultLimitWhenNull() {
            when(shelfRepository.findPersonalisedProducts("S-1", null, null))
                    .thenReturn(Collections.emptyList());

            service.getProducts("S-1", null, null, null);

            verify(shelfRepository).findPersonalisedProducts("S-1", null, null);
        }
    }
}
