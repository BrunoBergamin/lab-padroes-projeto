package br.com.bergamin.patterns.rules;

import br.com.bergamin.patterns.order.OrderItem;
import br.com.bergamin.patterns.shipping.ShippingMode;

import java.math.BigDecimal;
import java.util.List;

/** Tudo o que as regras precisam ler para aprovar ou barrar o checkout. */
public record CheckoutContext(String customer,
                              List<OrderItem> items,
                              String zipCode,
                              String coupon,
                              ShippingMode mode) {

    public BigDecimal subtotal() {
        return items.stream()
                .map(OrderItem::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
