package com.ecommerce.cartservice.dto.response;

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
public class CartResponse {

    private UUID id;
    private String userId;
    private String sessionId;
    private String status;

    @Builder.Default
    private List<CartItemResponse> items = new ArrayList<>();

    private Integer totalItems;
    private BigDecimal subtotal;
    private BigDecimal discount;
    private BigDecimal total;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

