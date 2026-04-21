package com.ecommerce.cartservice.event;

import com.ecommerce.cartservice.service.CartService;
import com.ecommerce.events.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventConsumer {

    private final CartService cartService;

    @KafkaListener(
            topics = "order.created",
            groupId = "cart-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleOrderCreated(
            OrderCreatedEvent event
    ) {
        log.info("Received OrderCreatedEvent: eventId={}, orderId={}, userId={}",
                event.getEventId(), event.getOrderId(), event.getUserId());

        cartService.clearCartByUserId(event.getUserId());
        log.info("Cleared cart for userId={}", event.getUserId());
    }
}


