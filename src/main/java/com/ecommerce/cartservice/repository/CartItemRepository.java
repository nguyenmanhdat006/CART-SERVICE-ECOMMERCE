package com.ecommerce.cartservice.repository;

import com.ecommerce.cartservice.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, UUID> {

    List<CartItem> findByCartId(UUID cartId);

    Optional<CartItem> findByCartIdAndProductId(UUID cartId, String productId);

    Optional<CartItem> findByCartIdAndProductIdAndProductVariantId(UUID cartId, String productId, String variantId);

    void deleteByCartId(UUID cartId);

    @Query("SELECT SUM(ci.quantity) FROM CartItem ci WHERE ci.cart.id = :cartId")
    Integer getTotalItemsByCartId(@Param("cartId") UUID cartId);
}

