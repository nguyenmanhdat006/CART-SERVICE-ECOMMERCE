package com.ecommerce.cartservice.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemResponse {

    private UUID id;
    private String productId;
    private String productVariantId;
    private String productName;
    private String productImageUrl;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal total;
    private Boolean inStock;
    private LocalDateTime createdAt;
}

