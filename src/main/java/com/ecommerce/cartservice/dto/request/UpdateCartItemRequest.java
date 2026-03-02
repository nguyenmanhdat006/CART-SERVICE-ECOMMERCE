package com.ecommerce.cartservice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update a cart item quantity")
public class UpdateCartItemRequest {

    @Schema(description = "New quantity for the cart item", example = "5", required = true, minimum = "0", maximum = "100")
    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity must be at least 0")
    @Max(value = 100, message = "Quantity cannot exceed 100")
    private Integer quantity;
}

