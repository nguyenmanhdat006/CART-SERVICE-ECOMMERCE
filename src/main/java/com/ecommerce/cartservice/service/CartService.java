package com.ecommerce.cartservice.service;

import com.ecommerce.cartservice.dto.request.AddToCartRequest;
import com.ecommerce.cartservice.dto.request.UpdateCartItemRequest;
import com.ecommerce.cartservice.dto.response.CartItemResponse;
import com.ecommerce.cartservice.dto.response.CartResponse;
import com.ecommerce.cartservice.dto.response.CartSummaryResponse;
import com.ecommerce.cartservice.entity.Cart;
import com.ecommerce.cartservice.entity.CartItem;
import com.ecommerce.cartservice.exception.ResourceNotFoundException;
import com.ecommerce.cartservice.mapper.CartMapper;
import com.ecommerce.cartservice.model.RedisCart;
import com.ecommerce.cartservice.model.RedisCartItem;
import com.ecommerce.cartservice.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemService cartItemService;
    private final CartMapper cartMapper;
    private final RedisCartService redisCartService;

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            return jwt.getSubject();
        }
        throw new RuntimeException("Unable to get user ID from security context");
    }

    public CartResponse getCurrentCart() {
        String userId = getCurrentUserId();
        log.debug("Getting cart for user: {}", userId);

        // Try to get from Redis first
        RedisCart redisCart = redisCartService.getCart(userId);
        if (redisCart != null) {
            log.debug("Cart found in Redis cache for user: {}", userId);
            // Still need to load from DB for full entity data
        }

        // Load from DB
        Cart cart = cartRepository.findByUserIdAndStatus(userId, Cart.CartStatus.ACTIVE)
                .orElseGet(() -> {
                    log.debug("Creating new cart for user: {}", userId);
                    Cart newCart = Cart.builder()
                            .userId(userId)
                            .status(Cart.CartStatus.ACTIVE)
                            .items(new ArrayList<>())
                            .build();
                    return cartRepository.save(newCart);
                });

        // Validate items
        if (!cart.getItems().isEmpty()) {
            cartItemService.validateCartItems(cart);
        }

        // Sync to Redis
        syncToRedis(cart);

        return buildCartResponse(cart);
    }

    public CartResponse getGuestCart(String sessionId) {
        log.debug("Getting guest cart for session: {}", sessionId);

        // Try Redis first
        RedisCart redisCart = redisCartService.getCart(sessionId);
        if (redisCart != null) {
            log.debug("Guest cart found in Redis cache");
        }

        // Load from DB
        Cart cart = cartRepository.findBySessionIdAndStatus(sessionId, Cart.CartStatus.ACTIVE)
                .orElseGet(() -> {
                    log.debug("Creating new guest cart for session: {}", sessionId);
                    Cart newCart = Cart.builder()
                            .userId("guest")
                            .sessionId(sessionId)
                            .status(Cart.CartStatus.ACTIVE)
                            .items(new ArrayList<>())
                            .build();
                    return cartRepository.save(newCart);
                });

        syncToRedis(cart);
        return buildCartResponse(cart);
    }

    public CartResponse addToCart(AddToCartRequest request) {
        String userId = getCurrentUserId();
        log.debug("Adding item to cart for user: {}", userId);

        Cart cart = cartRepository.findByUserIdAndStatus(userId, Cart.CartStatus.ACTIVE)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                            .userId(userId)
                            .status(Cart.CartStatus.ACTIVE)
                            .items(new ArrayList<>())
                            .build();
                    return cartRepository.save(newCart);
                });

        // Add or update item
        cartItemService.addOrUpdateItem(cart, request.getProductId(), request.getProductVariantId(), request.getQuantity());

        // Refresh cart
        cart = cartRepository.findById(cart.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        // Update Redis cache
        syncToRedis(cart);

        return buildCartResponse(cart);
    }

    public CartResponse updateCartItem(UUID itemId, UpdateCartItemRequest request) {
        log.debug("Updating cart item: {}", itemId);

        CartItem item = cartItemService.getCartItems(null).stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + itemId));

        cartItemService.updateQuantity(itemId, request.getQuantity());

        // Refresh cart
        Cart cart = cartRepository.findById(item.getCart().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        // Update Redis
        syncToRedis(cart);

        return buildCartResponse(cart);
    }

    public void removeCartItem(UUID itemId) {
        log.debug("Removing cart item: {}", itemId);

        CartItem item = cartItemService.getCartItems(null).stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + itemId));

        UUID cartId = item.getCart().getId();
        cartItemService.removeItem(itemId);

        // Update Redis
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
        syncToRedis(cart);
    }

    public void clearCart() {
        String userId = getCurrentUserId();
        log.debug("Clearing cart for user: {}", userId);

        Cart cart = cartRepository.findByUserIdAndStatus(userId, Cart.CartStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Active cart not found for user"));

        cart.clearItems();
        cartRepository.save(cart);

        // Clear Redis cache
        redisCartService.clearCart(userId);
    }

    public CartSummaryResponse getCartSummary() {
        String userId = getCurrentUserId();
        log.debug("Getting cart summary for user: {}", userId);

        Cart cart = cartRepository.findByUserIdAndStatus(userId, Cart.CartStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Active cart not found for user"));

        BigDecimal subtotal = calculateSubtotal(cart.getItems());
        BigDecimal discount = BigDecimal.ZERO; // TODO: Implement discount calculation
        BigDecimal shipping = BigDecimal.ZERO; // TODO: Implement shipping calculation
        BigDecimal tax = BigDecimal.ZERO; // TODO: Implement tax calculation
        BigDecimal total = subtotal.subtract(discount).add(shipping).add(tax);

        Integer totalItems = cart.getItems().stream()
                .mapToInt(CartItem::getQuantity)
                .sum();

        return CartSummaryResponse.builder()
                .totalItems(totalItems)
                .subtotal(subtotal)
                .discount(discount)
                .shipping(shipping)
                .tax(tax)
                .total(total)
                .build();
    }

    public CartResponse mergeGuestCart(String guestSessionId, String userId) {
        log.debug("Merging guest cart {} to user cart {}", guestSessionId, userId);

        // Find guest cart
        Cart guestCart = cartRepository.findBySessionIdAndStatus(guestSessionId, Cart.CartStatus.ACTIVE)
                .orElse(null);

        if (guestCart == null || guestCart.getItems().isEmpty()) {
            log.debug("No guest cart to merge");
            return getCurrentCart();
        }

        // Find or create user cart
        Cart userCart = cartRepository.findByUserIdAndStatus(userId, Cart.CartStatus.ACTIVE)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                            .userId(userId)
                            .status(Cart.CartStatus.ACTIVE)
                            .items(new ArrayList<>())
                            .build();
                    return cartRepository.save(newCart);
                });

        // Merge items
        for (CartItem guestItem : guestCart.getItems()) {
            try {
                cartItemService.addOrUpdateItem(
                        userCart,
                        guestItem.getProductId(),
                        guestItem.getProductVariantId(),
                        guestItem.getQuantity()
                );
            } catch (Exception e) {
                log.error("Error merging item from guest cart: {}", e.getMessage());
            }
        }

        // Delete guest cart
        cartRepository.delete(guestCart);
        redisCartService.deleteCart(guestSessionId);

        // Refresh user cart
        userCart = cartRepository.findById(userCart.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        // Update Redis
        syncToRedis(userCart);

        return buildCartResponse(userCart);
    }

    private CartResponse buildCartResponse(Cart cart) {
        CartResponse response = cartMapper.toResponse(cart);

        // Calculate totals
        BigDecimal subtotal = calculateSubtotal(cart.getItems());
        Integer totalItems = cart.getItems().stream()
                .mapToInt(CartItem::getQuantity)
                .sum();

        response.setSubtotal(subtotal);
        response.setDiscount(BigDecimal.ZERO);
        response.setTotal(subtotal);
        response.setTotalItems(totalItems);

        // Calculate item totals
        for (CartItemResponse itemResponse : response.getItems()) {
            itemResponse.setTotal(itemResponse.getPrice().multiply(BigDecimal.valueOf(itemResponse.getQuantity())));
            itemResponse.setInStock(true); // TODO: Check actual stock status
        }

        return response;
    }

    private BigDecimal calculateSubtotal(List<CartItem> items) {
        return items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void syncToRedis(Cart cart) {
        try {
            String key = cart.getUserId().equals("guest") ? cart.getSessionId() : cart.getUserId();

            List<RedisCartItem> redisItems = new ArrayList<>();
            for (CartItem item : cart.getItems()) {
                RedisCartItem redisItem = RedisCartItem.builder()
                        .productId(item.getProductId())
                        .productVariantId(item.getProductVariantId())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .productName(item.getProductName())
                        .productImageUrl(item.getProductImageUrl())
                        .build();
                redisItems.add(redisItem);
            }

            RedisCart redisCart = RedisCart.builder()
                    .key(key)
                    .items(redisItems)
                    .build();

            redisCartService.saveCart(key, redisCart);
            log.debug("Cart synced to Redis for key: {}", key);
        } catch (Exception e) {
            log.error("Error syncing cart to Redis: {}", e.getMessage());
        }
    }
}

