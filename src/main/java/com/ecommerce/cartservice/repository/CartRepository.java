package com.ecommerce.cartservice.repository;

import com.ecommerce.cartservice.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartRepository extends JpaRepository<Cart, UUID> {

    Optional<Cart> findByUserIdAndStatus(String userId, Cart.CartStatus status);

    Optional<Cart> findBySessionIdAndStatus(String sessionId, Cart.CartStatus status);

    List<Cart> findByStatusAndUpdatedAtBefore(Cart.CartStatus status, LocalDateTime date);

    void deleteByUserId(String userId);
}

