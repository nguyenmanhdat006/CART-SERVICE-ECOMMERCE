package com.ecommerce.cartservice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Shopping cart response containing items and pricing information")
public class CartResponse {

    @Schema(description = "Unique identifier of the cart", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "User ID associated with the cart", example = "user-123")
    private String userId;

    @Schema(description = "Session ID for guest carts", example = "sess-12345")
    private String sessionId;

    @Schema(description = "Status of the cart", example = "ACTIVE", allowableValues = {"ACTIVE", "ARCHIVED"})
    private String status;

    @Schema(description = "List of items in the cart")
    @Builder.Default
    private List<CartItemResponse> items = new ArrayList<>();

    @Schema(description = "Total number of items in the cart", example = "3")
    private Integer totalItems;

    @Schema(description = "Subtotal price before discounts", example = "150.00")
    private BigDecimal subtotal;

    @Schema(description = "Total discount amount", example = "10.00")
    private BigDecimal discount;

    @Schema(description = "Total price after discounts", example = "140.00")
    private BigDecimal total;

    @Schema(description = "Timestamp when the cart was created")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp when the cart was last updated")
    private LocalDateTime updatedAt;
}

