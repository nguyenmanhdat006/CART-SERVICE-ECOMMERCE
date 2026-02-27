package com.ecommerce.cartservice.model;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RedisCartItem implements Serializable {

    private static final long serialVersionUID = 1L;

    private String productId;
    private String productVariantId;
    private Integer quantity;
    private BigDecimal price;
    private String productName;
    private String productImageUrl;
}

