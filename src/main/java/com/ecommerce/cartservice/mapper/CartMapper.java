package com.ecommerce.cartservice.mapper;

import com.ecommerce.cartservice.dto.response.CartItemResponse;
import com.ecommerce.cartservice.dto.response.CartResponse;
import com.ecommerce.cartservice.entity.Cart;
import com.ecommerce.cartservice.entity.CartItem;
import com.ecommerce.cartservice.model.RedisCart;
import com.ecommerce.cartservice.model.RedisCartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface CartMapper {

    @Mapping(target = "status", expression = "java(cart.getStatus().name())")
    @Mapping(target = "items", source = "items")
    @Mapping(target = "totalItems", expression = "java(calculateTotalItems(cart))")
    @Mapping(target = "subtotal", expression = "java(calculateSubtotal(cart))")
    @Mapping(target = "discount", expression = "java(java.math.BigDecimal.ZERO)")
    @Mapping(target = "total", expression = "java(calculateSubtotal(cart))")
    CartResponse toResponse(Cart cart);

    @Mapping(target = "total", expression = "java(calculateItemTotal(item))")
    @Mapping(target = "inStock", constant = "true")
    CartItemResponse toItemResponse(CartItem item);

    List<CartItemResponse> toItemResponseList(List<CartItem> items);

    default Integer calculateTotalItems(Cart cart) {
        if (cart == null || cart.getItems() == null) {
            return 0;
        }
        return cart.getItems().stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    default BigDecimal calculateSubtotal(Cart cart) {
        if (cart == null || cart.getItems() == null) {
            return BigDecimal.ZERO;
        }
        return cart.getItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    default BigDecimal calculateItemTotal(CartItem item) {
        if (item == null || item.getPrice() == null) {
            return BigDecimal.ZERO;
        }
        return item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
    }

    // Redis mappings
    default RedisCart toRedisCart(Cart cart) {
        if (cart == null) {
            return null;
        }

        String key = cart.getUserId() != null && !cart.getUserId().equals("guest")
                ? cart.getUserId()
                : cart.getSessionId();

        List<RedisCartItem> redisItems = new ArrayList<>();
        if (cart.getItems() != null) {
            redisItems = cart.getItems().stream()
                    .map(this::toRedisCartItem)
                    .collect(Collectors.toList());
        }

        return RedisCart.builder()
                .key(key)
                .items(redisItems)
                .build();
    }

    default RedisCartItem toRedisCartItem(CartItem item) {
        if (item == null) {
            return null;
        }

        return RedisCartItem.builder()
                .productId(item.getProductId())
                .productVariantId(item.getProductVariantId())
                .quantity(item.getQuantity())
                .price(item.getPrice())
                .productName(item.getProductName())
                .productImageUrl(item.getProductImageUrl())
                .build();
    }

    default Cart fromRedisCart(RedisCart redisCart) {
        if (redisCart == null) {
            return null;
        }

        Cart cart = Cart.builder()
                .status(Cart.CartStatus.ACTIVE)
                .items(new ArrayList<>())
                .build();

        // Determine if it's a user cart or guest cart
        if (redisCart.getKey() != null) {
            if (redisCart.getKey().contains("-")) {
                // Likely a session ID (UUID format with dashes)
                cart.setSessionId(redisCart.getKey());
                cart.setUserId("guest");
            } else {
                // User ID
                cart.setUserId(redisCart.getKey());
            }
        }

        return cart;
    }
}

