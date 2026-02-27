package com.ecommerce.cartservice.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartSummaryResponse {

    private Integer totalItems;
    private BigDecimal subtotal;
    private BigDecimal discount;
    private BigDecimal shipping;
    private BigDecimal tax;
    private BigDecimal total;
}

