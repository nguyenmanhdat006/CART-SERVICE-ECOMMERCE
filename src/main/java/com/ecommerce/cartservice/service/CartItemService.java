package com.ecommerce.cartservice.service;

import com.ecommerce.cartservice.client.ProductServiceClient;
import com.ecommerce.cartservice.dto.response.ProductResponse;
import com.ecommerce.cartservice.entity.Cart;
import com.ecommerce.cartservice.entity.CartItem;
import com.ecommerce.cartservice.exception.BadRequestException;
import com.ecommerce.cartservice.exception.ResourceNotFoundException;
import com.ecommerce.cartservice.repository.CartItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CartItemService {

    private final CartItemRepository cartItemRepository;
    private final ProductServiceClient productServiceClient;

    @Value("${cart.max-items-per-cart:50}")
    private Integer maxItemsPerCart;

    public CartItem addOrUpdateItem(Cart cart, String productId, String variantId, Integer quantity) {
        log.debug("Adding/updating item to cart: productId={}, variantId={}, quantity={}", productId, variantId, quantity);

        // Validate product exists and get details
        ProductResponse product = productServiceClient.getProduct(productId);
        if (product == null) {
            throw new ResourceNotFoundException("Product not found with id: " + productId);
        }

        // Check stock availability
        if (!hasSufficientStock(product, quantity)) {
            log.warn("Stock check failed for product {}: requestedQuantity={}, stockQuantity={}",
                    productId, quantity, product.getStockQuantity());
            throw new BadRequestException("Product is out of stock");
        }

        // Check if item already exists
        CartItem existingItem;
        if (variantId != null) {
            existingItem = cartItemRepository.findByCartIdAndProductIdAndProductVariantId(cart.getId(), productId, variantId)
                    .orElse(null);
        } else {
            existingItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                    .orElse(null);
        }

        if (existingItem != null) {
            // Update existing item
            int newQuantity = existingItem.getQuantity() + quantity;

            if (newQuantity > product.getStockQuantity()) {
                throw new BadRequestException("Requested quantity exceeds available stock");
            }

            existingItem.setQuantity(newQuantity);
            existingItem.setPrice(product.getSalePrice() != null ? product.getSalePrice() : product.getPrice());
            existingItem.setProductName(product.getName());
            existingItem.setProductImageUrl(product.getImageUrl());

            log.debug("Updated existing cart item: {}", existingItem.getId());
            return cartItemRepository.save(existingItem);
        } else {
            // Check max items limit
            int maxItemsLimit = maxItemsPerCart != null ? maxItemsPerCart : 50;
            Integer currentItemCount = cartItemRepository.getTotalItemsByCartId(cart.getId());
            if (currentItemCount != null && currentItemCount >= maxItemsLimit) {
                throw new BadRequestException("Cart has reached maximum items limit of " + maxItemsLimit);
            }

            // Create new item
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .productId(productId)
                    .productVariantId(variantId)
                    .quantity(quantity)
                    .price(product.getSalePrice() != null ? product.getSalePrice() : product.getPrice())
                    .productName(product.getName())
                    .productImageUrl(product.getImageUrl())
                    .build();

            cart.addItem(newItem);

            log.debug("Created new cart item for product: {}", productId);
            return cartItemRepository.save(newItem);
        }
    }

    public void removeItem(UUID cartItemId) {
        log.debug("Removing cart item: {}", cartItemId);

        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + cartItemId));

        cartItemRepository.delete(item);
        log.debug("Cart item removed: {}", cartItemId);
    }

    public void updateQuantity(UUID cartItemId, Integer quantity) {
        log.debug("Updating cart item quantity: itemId={}, quantity={}", cartItemId, quantity);

        if (quantity < 0) {
            throw new BadRequestException("Quantity cannot be negative");
        }

        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + cartItemId));

        if (quantity == 0) {
            // Remove item if quantity is 0
            cartItemRepository.delete(item);
            log.debug("Cart item removed (quantity=0): {}", cartItemId);
        } else {
            // Check stock availability
            if (!productServiceClient.checkStock(item.getProductId(), quantity)) {
                throw new BadRequestException("Requested quantity exceeds available stock");
            }

            item.setQuantity(quantity);
            cartItemRepository.save(item);
            log.debug("Cart item quantity updated: {}", cartItemId);
        }
    }

    public List<CartItem> getCartItems(UUID cartId) {
        log.debug("Getting cart items for cart: {}", cartId);
        return cartItemRepository.findByCartId(cartId);
    }

    public CartItem getCartItemById(UUID cartItemId) {
        log.debug("Getting cart item by id: {}", cartItemId);
        return cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + cartItemId));
    }

    public void validateCartItems(Cart cart) {
        log.debug("Validating cart items for cart: {}", cart.getId());

        List<CartItem> items = cart.getItems();
        boolean updated = false;

        for (CartItem item : items) {
            try {
                ProductResponse product = productServiceClient.getProduct(item.getProductId());

                // Check if product is still in stock
                if (isOutOfStock(product)) {
                    log.warn("Product {} is out of stock, will be marked", item.getProductId());
                    continue;
                }

                // Check if quantity is still available
                if (product.getStockQuantity() < item.getQuantity()) {
                    log.warn("Product {} has insufficient stock, adjusting quantity from {} to {}",
                            item.getProductId(), item.getQuantity(), product.getStockQuantity());
                    item.setQuantity(product.getStockQuantity());
                    updated = true;
                }

                // Update price if changed
                BigDecimal currentPrice = product.getSalePrice() != null ? product.getSalePrice() : product.getPrice();
                if (!item.getPrice().equals(currentPrice)) {
                    log.debug("Updating price for product {} from {} to {}",
                            item.getProductId(), item.getPrice(), currentPrice);
                    item.setPrice(currentPrice);
                    updated = true;
                }

            } catch (Exception e) {
                log.error("Error validating cart item {}: {}", item.getId(), e.getMessage());
            }
        }

        if (updated) {
            cartItemRepository.saveAll(items);
            log.debug("Cart items validated and updated");
        }
    }

    private boolean hasSufficientStock(ProductResponse product, Integer quantity) {
        if (product == null || quantity == null || quantity <= 0) {
            return false;
        }

        Integer stockQuantity = product.getStockQuantity();
        return stockQuantity != null && stockQuantity != 0;
    }

    private boolean isOutOfStock(ProductResponse product) {
        return product == null || product.getStockQuantity() == null || product.getStockQuantity() <= 0;
    }

    public void deleteCartItemsByCartId(UUID cartId) {
        log.debug("Deleting all cart items for cart: {}", cartId);
        cartItemRepository.deleteByCartId(cartId);
        log.debug("All cart items deleted for cart: {}", cartId);
    }
}

