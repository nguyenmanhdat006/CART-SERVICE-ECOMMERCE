package com.ecommerce.cartservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MergeCartRequest {

    @NotBlank(message = "Guest session ID is required")
    private String guestSessionId;
}

