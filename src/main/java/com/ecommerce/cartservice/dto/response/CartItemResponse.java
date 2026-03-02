package com.ecommerce.cartservice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "An individual item in the shopping cart")
public class CartItemResponse {

    @Schema(description = "Unique identifier of the cart item", example = "550e8400-e29b-41d4-a716-446655440001")
    private UUID id;

    @Schema(description = "Product ID", example = "PROD-001")
    private String productId;

    @Schema(description = "Product variant ID", example = "VAR-001")
    private String productVariantId;

    @Schema(description = "Name of the product", example = "Laptop")
    private String productName;

    @Schema(description = "Product image URL", example = "https://cdn.example.com/product-image.jpg")
    private String productImageUrl;

    @Schema(description = "Quantity of the product in cart", example = "2")
    private Integer quantity;

    @Schema(description = "Price per unit", example = "75.00")
    private BigDecimal price;

    @Schema(description = "Total price for this item (price * quantity)", example = "150.00")
    private BigDecimal total;

    @Schema(description = "Whether the product is in stock", example = "true")
    private Boolean inStock;

    @Schema(description = "Timestamp when the item was added to cart")
    private LocalDateTime createdAt;
}

