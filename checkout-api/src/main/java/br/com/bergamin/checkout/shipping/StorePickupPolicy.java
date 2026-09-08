package br.com.bergamin.checkout.shipping;

import br.com.bergamin.checkout.order.OrderItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class StorePickupPolicy implements ShippingPolicy {

    @Override
    public ShippingMode mode() {
        return ShippingMode.PICKUP;
    }

    @Override
    public Shipping quote(List<OrderItem> items, String zipCode) {
        return new Shipping("Retirada na loja", BigDecimal.ZERO, 0);
    }
}
