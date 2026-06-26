package com.personalized.api.service;

import com.personalized.api.model.ProductResponse;
import com.personalized.api.repository.ShopperShelfRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExternalQueryService {

    private final ShopperShelfRepository shelfRepository;

    @Value("${app.external.max-limit:100}")
    private int maxLimit;

    @Value("${app.external.default-limit:10}")
    private int defaultLimit;

    public List<ProductResponse> getProducts(String shopperId, String category, String brand, Integer limit) {

        int effectiveLimit = resolveLimit(limit);
        log.debug("Cache MISS — querying DB: shopper={} cat={} brand={} limit={}",
                shopperId, category, brand, effectiveLimit);

        List<ProductResponse> results =
                shelfRepository.findPersonalisedProducts(shopperId, category, brand);

        // Slice in Java — keeps the JPQL portable across H2 and PostgreSQL
        return results.size() <= effectiveLimit
                ? results
                : results.subList(0, effectiveLimit);
    }

    int resolveLimit(Integer requested) {
        if (requested == null || requested <= 0) return defaultLimit;
        return Math.min(requested, maxLimit);
    }
}
