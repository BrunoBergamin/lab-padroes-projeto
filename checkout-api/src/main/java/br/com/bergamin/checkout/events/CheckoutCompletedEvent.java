package br.com.bergamin.checkout.events;

import br.com.bergamin.checkout.order.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Fato consumado: um checkout foi concluido. */
public record CheckoutCompletedEvent(UUID orderId,
                                     String customer,
                                     BigDecimal total,
                                     OrderStatus status,
                                     Instant occurredAt) {
}
