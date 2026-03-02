package com.ecommerce.cartservice.controller;

import com.ecommerce.cartservice.dto.request.AddToCartRequest;
import com.ecommerce.cartservice.dto.request.MergeCartRequest;
import com.ecommerce.cartservice.dto.request.UpdateCartItemRequest;
import com.ecommerce.cartservice.dto.response.ApiResponse;
import com.ecommerce.cartservice.dto.response.CartResponse;
import com.ecommerce.cartservice.dto.response.CartSummaryResponse;
import com.ecommerce.cartservice.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Cart Management", description = "APIs for managing shopping cart operations")
public class CartController {

    private final CartService cartService;

    @Operation(summary = "Get current cart", description = "Retrieves the shopping cart for the authenticated user")
    @SecurityRequirement(name = "Bearer JWT")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cart retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid token")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCurrentCart(Authentication authentication) {
        log.debug("Getting current cart for user: {}", authentication.getName());
        CartResponse cart = cartService.getCurrentCart();
        return ResponseEntity.ok(ApiResponse.success(cart));
    }

    @Operation(summary = "Get guest cart", description = "Retrieves a guest shopping cart by session ID")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Guest cart retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Cart not found")
    })
    @GetMapping("/guest/{sessionId}")
    public ResponseEntity<ApiResponse<CartResponse>> getGuestCart(
            @Parameter(description = "Session ID of the guest cart") @PathVariable String sessionId) {
        log.debug("Getting guest cart for session: {}", sessionId);
        CartResponse cart = cartService.getGuestCart(sessionId);
        return ResponseEntity.ok(ApiResponse.success(cart));
    }

    @Operation(summary = "Add item to cart", description = "Adds a product item to the shopping cart")
    @SecurityRequirement(name = "Bearer JWT")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Item added to cart successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartResponse>> addToCart(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Item to add to cart")
            @Valid @RequestBody AddToCartRequest request,
            Authentication authentication) {
        log.debug("Adding item to cart: productId={}, quantity={}",
                request.getProductId(), request.getQuantity());

        CartResponse cart = cartService.addToCart(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Item added to cart successfully", cart));
    }

    @Operation(summary = "Update cart item", description = "Updates the quantity of an item in the shopping cart")
    @SecurityRequirement(name = "Bearer JWT")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cart item updated successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Item not found")
    })
    @PutMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<CartResponse>> updateCartItem(
            @Parameter(description = "ID of the cart item to update") @PathVariable UUID itemId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Updated item data")
            @Valid @RequestBody UpdateCartItemRequest request,
            Authentication authentication) {
        log.debug("Updating cart item: itemId={}, quantity={}", itemId, request.getQuantity());

        CartResponse cart = cartService.updateCartItem(itemId, request);
        return ResponseEntity.ok(ApiResponse.success("Cart item updated successfully", cart));
    }

    @Operation(summary = "Remove cart item", description = "Removes an item from the shopping cart")
    @SecurityRequirement(name = "Bearer JWT")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Item removed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Item not found")
    })
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> removeCartItem(
            @Parameter(description = "ID of the cart item to remove") @PathVariable UUID itemId,
            Authentication authentication) {
        log.debug("Removing cart item: {}", itemId);

        cartService.removeCartItem(itemId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Clear cart", description = "Clears all items from the shopping cart")
    @SecurityRequirement(name = "Bearer JWT")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Cart cleared successfully")
    })
    @DeleteMapping
    public ResponseEntity<Void> clearCart(Authentication authentication) {
        log.debug("Clearing cart for user: {}", authentication.getName());

        cartService.clearCart();
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get cart summary", description = "Retrieves a summary of the shopping cart (total price, item count, etc.)")
    @SecurityRequirement(name = "Bearer JWT")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cart summary retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<CartSummaryResponse>> getCartSummary(Authentication authentication) {
        log.debug("Getting cart summary for user: {}", authentication.getName());

        CartSummaryResponse summary = cartService.getCartSummary();
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    @Operation(summary = "Merge guest cart", description = "Merges a guest shopping cart with the authenticated user's cart")
    @SecurityRequirement(name = "Bearer JWT")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Carts merged successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Guest cart not found")
    })
    @PostMapping("/merge")
    public ResponseEntity<ApiResponse<CartResponse>> mergeGuestCart(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Guest cart merge request")
            @Valid @RequestBody MergeCartRequest request,
            Authentication authentication) {
        log.debug("Merging guest cart to user cart: guestSessionId={}, userId={}",
                request.getGuestSessionId(), authentication.getName());

        String userId = authentication.getName();
        CartResponse cart = cartService.mergeGuestCart(request.getGuestSessionId(), userId);

        return ResponseEntity.ok(ApiResponse.success("Carts merged successfully", cart));
    }
}

