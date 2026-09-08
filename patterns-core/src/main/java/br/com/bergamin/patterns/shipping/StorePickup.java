package br.com.bergamin.patterns.shipping;

import br.com.bergamin.patterns.order.OrderItem;

import java.math.BigDecimal;
import java.util.List;

/** Retirada na loja: nao ha frete a cobrar. */
public final class StorePickup implements ShippingPolicy {

    @Override
    public ShippingMode mode() {
        return ShippingMode.PICKUP;
    }

    @Override
    public Shipping quote(List<OrderItem> items, String zipCode) {
        return new Shipping("Retirada na loja", BigDecimal.ZERO, 0);
    }
}
