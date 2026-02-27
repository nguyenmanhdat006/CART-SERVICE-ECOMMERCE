package com.ecommerce.cartservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RedisHash("cart")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RedisCart implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String key; // userId or sessionId

    @Builder.Default
    private List<RedisCartItem> items = new ArrayList<>();

    private LocalDateTime lastUpdated;
}

