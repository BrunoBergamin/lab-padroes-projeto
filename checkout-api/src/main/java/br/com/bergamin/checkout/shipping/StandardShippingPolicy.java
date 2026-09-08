package br.com.bergamin.checkout.shipping;

import br.com.bergamin.checkout.order.OrderItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class StandardShippingPolicy implements ShippingPolicy {

    private static final BigDecimal BASE = new BigDecimal("19.90");
    private static final BigDecimal PER_UNIT = new BigDecimal("1.50");

    @Override
    public ShippingMode mode() {
        return ShippingMode.STANDARD;
    }

    @Override
    public Shipping quote(List<OrderItem> items, String zipCode) {
        BigDecimal cost = BASE.add(PER_UNIT.multiply(BigDecimal.valueOf(totalUnits(items))));
        return new Shipping("Entrega economica", cost, ZipCodes.isSoutheast(zipCode) ? 5 : 9);
    }
}
