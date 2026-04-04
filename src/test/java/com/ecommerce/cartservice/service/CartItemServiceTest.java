package com.ecommerce.cartservice.service;

import com.ecommerce.cartservice.client.ProductServiceClient;
import com.ecommerce.cartservice.dto.response.ProductResponse;
import com.ecommerce.cartservice.entity.Cart;
import com.ecommerce.cartservice.entity.CartItem;
import com.ecommerce.cartservice.exception.BadRequestException;
import com.ecommerce.cartservice.repository.CartItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartItemServiceTest {

	@Mock
	private CartItemRepository cartItemRepository;

	@Mock
	private ProductServiceClient productServiceClient;

	@InjectMocks
	private CartItemService cartItemService;

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(cartItemService, "maxItemsPerCart", 50);
	}

	@Test
	void addOrUpdateItem_shouldAllowWhenStockQuantityIsSufficient() {
		Cart cart = Cart.builder()
				.id(UUID.randomUUID())
				.userId("user-1")
				.items(new ArrayList<>())
				.build();

		ProductResponse product = ProductResponse.builder()
				.id("product-1")
				.name("Product 1")
				.price(BigDecimal.TEN)
				.stockQuantity(100)
				.build();

		when(productServiceClient.getProduct("product-1")).thenReturn(product);
		when(cartItemRepository.findByCartIdAndProductId(cart.getId(), "product-1")).thenReturn(Optional.empty());
		when(cartItemRepository.getTotalItemsByCartId(cart.getId())).thenReturn(0);
		when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

		CartItem savedItem = cartItemService.addOrUpdateItem(cart, "product-1", null, 1);

		assertThat(savedItem.getQuantity()).isEqualTo(1);
		assertThat(savedItem.getProductId()).isEqualTo("product-1");
		verify(cartItemRepository).save(any(CartItem.class));
	}

	@Test
	void addOrUpdateItem_shouldRejectWhenStockQuantityIsNull() {
		Cart cart = Cart.builder()
				.id(UUID.randomUUID())
				.userId("user-1")
				.items(new ArrayList<>())
				.build();

		ProductResponse product = ProductResponse.builder()
				.id("product-1")
				.name("Product 1")
				.price(BigDecimal.TEN)
				.stockQuantity(null)
				.build();

		when(productServiceClient.getProduct("product-1")).thenReturn(product);

		assertThatThrownBy(() -> cartItemService.addOrUpdateItem(cart, "product-1", null, 1))
				.isInstanceOf(BadRequestException.class)
				.hasMessage("Product is out of stock");

		verify(cartItemRepository, never()).save(any(CartItem.class));
	}
}

