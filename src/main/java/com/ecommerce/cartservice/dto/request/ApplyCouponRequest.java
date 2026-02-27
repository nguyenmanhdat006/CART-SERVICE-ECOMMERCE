package com.ecommerce.cartservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApplyCouponRequest {

    @NotBlank(message = "Coupon code is required")
    private String couponCode;
}

