package br.com.bergamin.checkout.shipping;

import br.com.bergamin.checkout.order.OrderItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class ExpressShippingPolicy implements ShippingPolicy {

    private static final BigDecimal BASE = new BigDecimal("39.90");
    private static final BigDecimal PER_UNIT = new BigDecimal("2.50");

    @Override
    public ShippingMode mode() {
        return ShippingMode.EXPRESS;
    }

    @Override
    public Shipping quote(List<OrderItem> items, String zipCode) {
        BigDecimal cost = BASE.add(PER_UNIT.multiply(BigDecimal.valueOf(totalUnits(items))));
        return new Shipping("Entrega expressa", cost, ZipCodes.isSoutheast(zipCode) ? 1 : 3);
    }
}
