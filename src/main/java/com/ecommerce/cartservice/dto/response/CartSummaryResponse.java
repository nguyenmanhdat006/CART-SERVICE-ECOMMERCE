package com.ecommerce.cartservice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Summary of the shopping cart with pricing information")
public class CartSummaryResponse {

    @Schema(description = "Total number of items in the cart", example = "5")
    private Integer totalItems;

    @Schema(description = "Subtotal before any discounts", example = "250.00")
    private BigDecimal subtotal;

    @Schema(description = "Total discount amount applied", example = "20.00")
    private BigDecimal discount;

    @Schema(description = "Shipping cost", example = "10.00")
    private BigDecimal shipping;

    @Schema(description = "Tax amount", example = "21.75")
    private BigDecimal tax;

    @Schema(description = "Final total amount to be paid", example = "261.75")
    private BigDecimal total;
}

