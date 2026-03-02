package com.ecommerce.cartservice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to add an item to the shopping cart")
public class AddToCartRequest {

    @Schema(description = "Product ID", example = "PROD-001", required = true)
    @NotBlank(message = "Product ID is required")
    private String productId;

    @Schema(description = "Quantity of the product", example = "2", required = true, minimum = "1", maximum = "100")
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 100, message = "Quantity cannot exceed 100")
    private Integer quantity;

    @Schema(description = "Optional product variant ID for product variations", example = "VAR-001")
    private String productVariantId;
}

