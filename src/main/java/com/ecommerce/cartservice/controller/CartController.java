package com.ecommerce.cartservice.controller;

import com.ecommerce.cartservice.dto.request.AddToCartRequest;
import com.ecommerce.cartservice.dto.request.MergeCartRequest;
import com.ecommerce.cartservice.dto.request.UpdateCartItemRequest;
import com.ecommerce.cartservice.dto.response.ApiResponse;
import com.ecommerce.cartservice.dto.response.CartResponse;
import com.ecommerce.cartservice.dto.response.CartSummaryResponse;
import com.ecommerce.cartservice.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Slf4j
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCurrentCart(Authentication authentication) {
        log.debug("Getting current cart for user: {}", authentication.getName());
        CartResponse cart = cartService.getCurrentCart();
        return ResponseEntity.ok(ApiResponse.success(cart));
    }

    @GetMapping("/guest/{sessionId}")
    public ResponseEntity<ApiResponse<CartResponse>> getGuestCart(@PathVariable String sessionId) {
        log.debug("Getting guest cart for session: {}", sessionId);
        CartResponse cart = cartService.getGuestCart(sessionId);
        return ResponseEntity.ok(ApiResponse.success(cart));
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartResponse>> addToCart(
            @Valid @RequestBody AddToCartRequest request,
            Authentication authentication) {
        log.debug("Adding item to cart: productId={}, quantity={}",
                request.getProductId(), request.getQuantity());

        CartResponse cart = cartService.addToCart(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Item added to cart successfully", cart));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<CartResponse>> updateCartItem(
            @PathVariable UUID itemId,
            @Valid @RequestBody UpdateCartItemRequest request,
            Authentication authentication) {
        log.debug("Updating cart item: itemId={}, quantity={}", itemId, request.getQuantity());

        CartResponse cart = cartService.updateCartItem(itemId, request);
        return ResponseEntity.ok(ApiResponse.success("Cart item updated successfully", cart));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> removeCartItem(
            @PathVariable UUID itemId,
            Authentication authentication) {
        log.debug("Removing cart item: {}", itemId);

        cartService.removeCartItem(itemId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(Authentication authentication) {
        log.debug("Clearing cart for user: {}", authentication.getName());

        cartService.clearCart();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<CartSummaryResponse>> getCartSummary(Authentication authentication) {
        log.debug("Getting cart summary for user: {}", authentication.getName());

        CartSummaryResponse summary = cartService.getCartSummary();
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    @PostMapping("/merge")
    public ResponseEntity<ApiResponse<CartResponse>> mergeGuestCart(
            @Valid @RequestBody MergeCartRequest request,
            Authentication authentication) {
        log.debug("Merging guest cart to user cart: guestSessionId={}, userId={}",
                request.getGuestSessionId(), authentication.getName());

        String userId = authentication.getName();
        CartResponse cart = cartService.mergeGuestCart(request.getGuestSessionId(), userId);

        return ResponseEntity.ok(ApiResponse.success("Carts merged successfully", cart));
    }
}

