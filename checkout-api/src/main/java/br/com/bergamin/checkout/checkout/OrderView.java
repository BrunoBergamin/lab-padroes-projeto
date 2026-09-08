package br.com.bergamin.checkout.checkout;

import br.com.bergamin.checkout.order.Order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Visao enxuta do pedido para as consultas e para as transicoes de status. */
public record OrderView(UUID id,
                        String cliente,
                        String status,
                        String entrega,
                        BigDecimal total,
                        Instant criadoEm) {

    public static OrderView of(Order order) {
        return new OrderView(order.getId(), order.getCustomer(), order.getStatus().label(),
                order.getShippingLabel(), order.getTotal(), order.getCreatedAt());
    }
}
