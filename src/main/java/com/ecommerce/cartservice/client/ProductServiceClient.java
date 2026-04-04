package com.ecommerce.cartservice.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper objectMapper;

    public ProductResponse getProduct(String productId) {
        try {
            log.debug("Fetching product with id: {}", productId);

            JsonNode responseNode = productServiceWebClient
                    .get()
                    .uri("/api/products/{productId}", productId)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .doOnError(error -> log.error("Error fetching product {}: {}", productId, error.getMessage()))
                    .onErrorResume(error -> Mono.error(
                            new ResourceNotFoundException("Product not found with id: " + productId)
                    ))
                    .block();

            if (responseNode == null) {
                throw new ResourceNotFoundException("Product not found with id: " + productId);
            }

            // Support both direct product payload and wrapped { success, data } payload.
            JsonNode productNode = responseNode.has("data") ? responseNode.get("data") : responseNode;
            ProductResponse product = objectMapper.convertValue(productNode, ProductResponse.class);

            if (product == null) {
                throw new ResourceNotFoundException("Product not found with id: " + productId);
            }

            log.debug("Fetched product {} with stockQuantity={}", productId, product.getStockQuantity());
            return product;
        } catch (Exception e) {
            log.error("Failed to fetch product {}: {}", productId, e.getMessage());
            throw new ResourceNotFoundException("Product not found with id: " + productId);
        }
    }

    public boolean checkStock(String productId, Integer quantity) {
        try {
            log.debug("Checking stock for product {} with quantity {}", productId, quantity);

            if (quantity == null || quantity <= 0) {
                return false;
            }

            ProductResponse product = getProduct(productId);

            if (product == null) {
                return false;
            }

            Integer stockQuantity = product.getStockQuantity();
            log.debug("Resolved stock for product {}: stockQuantity={}", productId, stockQuantity);

            return stockQuantity != null && stockQuantity != 0;
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

