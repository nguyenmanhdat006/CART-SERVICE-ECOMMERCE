package com.ecommerce.cartservice.service;

import com.ecommerce.cartservice.model.RedisCart;
import com.ecommerce.cartservice.model.RedisCartItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisCartService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${cart.redis-ttl-minutes:60}")
    private Integer redisTtlMinutes;

    private static final String CART_KEY_PREFIX = "cart:";

    private String getKey(String identifier) {
        return CART_KEY_PREFIX + identifier;
    }

    public RedisCart getCart(String identifier) {
        try {
            String key = getKey(identifier);
            Object value = redisTemplate.opsForValue().get(key);

            if (value instanceof RedisCart) {
                log.debug("Cart found in Redis for identifier: {}", identifier);
                return (RedisCart) value;
            }

            log.debug("Cart not found in Redis for identifier: {}", identifier);
            return null;
        } catch (Exception e) {
            log.error("Error getting cart from Redis for identifier {}: {}", identifier, e.getMessage());
            return null;
        }
    }

    public void saveCart(String identifier, RedisCart cart) {
        try {
            String key = getKey(identifier);
            cart.setLastUpdated(LocalDateTime.now());
            redisTemplate.opsForValue().set(key, cart, redisTtlMinutes, TimeUnit.MINUTES);
            log.debug("Cart saved to Redis for identifier: {} with TTL: {} minutes", identifier, redisTtlMinutes);
        } catch (Exception e) {
            log.error("Error saving cart to Redis for identifier {}: {}", identifier, e.getMessage());
        }
    }

    public void deleteCart(String identifier) {
        try {
            String key = getKey(identifier);
            redisTemplate.delete(key);
            log.debug("Cart deleted from Redis for identifier: {}", identifier);
        } catch (Exception e) {
            log.error("Error deleting cart from Redis for identifier {}: {}", identifier, e.getMessage());
        }
    }

    public void addItem(String identifier, RedisCartItem item) {
        try {
            RedisCart cart = getCart(identifier);

            if (cart == null) {
                cart = RedisCart.builder()
                        .key(identifier)
                        .items(new ArrayList<>())
                        .build();
            }

            // Check if item already exists
            boolean found = false;
            for (RedisCartItem existingItem : cart.getItems()) {
                if (existingItem.getProductId().equals(item.getProductId()) &&
                    (existingItem.getProductVariantId() == null && item.getProductVariantId() == null ||
                     existingItem.getProductVariantId() != null && existingItem.getProductVariantId().equals(item.getProductVariantId()))) {
                    existingItem.setQuantity(existingItem.getQuantity() + item.getQuantity());
                    found = true;
                    break;
                }
            }

            if (!found) {
                cart.getItems().add(item);
            }

            saveCart(identifier, cart);
            log.debug("Item added to cart in Redis for identifier: {}", identifier);
        } catch (Exception e) {
            log.error("Error adding item to cart in Redis for identifier {}: {}", identifier, e.getMessage());
        }
    }

    public void removeItem(String identifier, String productId) {
        try {
            RedisCart cart = getCart(identifier);

            if (cart != null) {
                cart.getItems().removeIf(item -> item.getProductId().equals(productId));
                saveCart(identifier, cart);
                log.debug("Item removed from cart in Redis for identifier: {}", identifier);
            }
        } catch (Exception e) {
            log.error("Error removing item from cart in Redis for identifier {}: {}", identifier, e.getMessage());
        }
    }

    public void clearCart(String identifier) {
        try {
            RedisCart cart = getCart(identifier);

            if (cart != null) {
                cart.getItems().clear();
                saveCart(identifier, cart);
                log.debug("Cart cleared in Redis for identifier: {}", identifier);
            }
        } catch (Exception e) {
            log.error("Error clearing cart in Redis for identifier {}: {}", identifier, e.getMessage());
        }
    }

    public Integer getTotalItems(String identifier) {
        try {
            RedisCart cart = getCart(identifier);

            if (cart != null) {
                return cart.getItems().stream()
                        .mapToInt(RedisCartItem::getQuantity)
                        .sum();
            }

            return 0;
        } catch (Exception e) {
            log.error("Error getting total items from Redis for identifier {}: {}", identifier, e.getMessage());
            return 0;
        }
    }
}

