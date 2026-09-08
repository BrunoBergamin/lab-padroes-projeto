package br.com.bergamin.patterns.events;

import java.math.BigDecimal;
import java.time.Instant;

/** Fato consumado: um checkout foi concluido. */
public record CheckoutEvent(String orderId,
                            String customer,
                            BigDecimal total,
                            String status,
                            Instant occurredAt) {
}
