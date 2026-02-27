package com.ecommerce.cartservice.client;

import com.ecommerce.cartservice.dto.response.ProductResponse;
import com.ecommerce.cartservice.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceClient {

    private final WebClient productServiceWebClient;

    public ProductResponse getProduct(String productId) {
        try {
            log.debug("Fetching product with id: {}", productId);

            return productServiceWebClient
                    .get()
                    .uri("/api/products/{productId}", productId)
                    .retrieve()
                    .bodyToMono(ProductResponse.class)
                    .doOnError(error -> log.error("Error fetching product {}: {}", productId, error.getMessage()))
                    .onErrorResume(error -> Mono.error(
                            new ResourceNotFoundException("Product not found with id: " + productId)
                    ))
                    .block();
        } catch (Exception e) {
            log.error("Failed to fetch product {}: {}", productId, e.getMessage());
            throw new ResourceNotFoundException("Product not found with id: " + productId);
        }
    }

    public boolean checkStock(String productId, Integer quantity) {
        try {
            log.debug("Checking stock for product {} with quantity {}", productId, quantity);

            ProductResponse product = getProduct(productId);

            if (product == null || !product.getInStock()) {
                return false;
            }

            return product.getStockQuantity() != null && product.getStockQuantity() >= quantity;
        } catch (Exception e) {
            log.error("Failed to check stock for product {}: {}", productId, e.getMessage());
            return false;
        }
    }

    public Map<String, ProductResponse> getProductsByIds(List<String> productIds) {
        Map<String, ProductResponse> productMap = new HashMap<>();

        if (productIds == null || productIds.isEmpty()) {
            return productMap;
        }

        log.debug("Fetching multiple products: {}", productIds);

        for (String productId : productIds) {
            try {
                ProductResponse product = getProduct(productId);
                if (product != null) {
                    productMap.put(productId, product);
                }
            } catch (Exception e) {
                log.error("Failed to fetch product {} in batch: {}", productId, e.getMessage());
            }
        }

        return productMap;
    }
}

