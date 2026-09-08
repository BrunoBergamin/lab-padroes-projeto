package br.com.bergamin.patterns.shipping;

import br.com.bergamin.patterns.order.OrderItem;

import java.math.BigDecimal;
import java.util.List;

/** Entrega expressa: custa mais e chega antes. */
public final class ExpressShipping implements ShippingPolicy {

    private static final BigDecimal BASE = new BigDecimal("39.90");
    private static final BigDecimal PER_UNIT = new BigDecimal("2.50");

    @Override
    public ShippingMode mode() {
        return ShippingMode.EXPRESS;
    }

    @Override
    public Shipping quote(List<OrderItem> items, String zipCode) {
        BigDecimal cost = BASE.add(PER_UNIT.multiply(BigDecimal.valueOf(totalUnits(items))));
        int days = ZipCodes.isSoutheast(zipCode) ? 1 : 3;
        return new Shipping("Entrega expressa", cost, days);
    }
}
