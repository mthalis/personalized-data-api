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
        log.info("action=get_products shopperId={} category={} brand={} limit={}",
                shopperId, category, brand, effectiveLimit);

        List<ProductResponse> results =
                shelfRepository.findPersonalisedProducts(shopperId, category, brand);

        List<ProductResponse> sliced = results.size() <= effectiveLimit
                ? results : results.subList(0, effectiveLimit);
        log.info("action=get_products_complete shopperId={} totalFound={} returned={}",
                shopperId, results.size(), sliced.size());
        return sliced;
    }

    int resolveLimit(Integer requested) {
        if (requested == null || requested <= 0) return defaultLimit;
        return Math.min(requested, maxLimit);
    }
}
