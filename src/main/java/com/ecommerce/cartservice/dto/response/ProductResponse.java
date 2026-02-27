package com.ecommerce.cartservice.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {

    private String id;
    private String name;
    private String slug;
    private BigDecimal price;
    private BigDecimal salePrice;
    private Integer stockQuantity;
    private Boolean inStock;
    private String imageUrl;
}

