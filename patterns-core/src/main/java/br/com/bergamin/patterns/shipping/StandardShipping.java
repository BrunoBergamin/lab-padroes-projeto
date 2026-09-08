package br.com.bergamin.patterns.shipping;

import br.com.bergamin.patterns.order.OrderItem;

import java.math.BigDecimal;
import java.util.List;

/** Entrega economica: base fixa mais um valor por unidade, prazo maior fora do Sudeste. */
public final class StandardShipping implements ShippingPolicy {

    private static final BigDecimal BASE = new BigDecimal("19.90");
    private static final BigDecimal PER_UNIT = new BigDecimal("1.50");

    @Override
    public ShippingMode mode() {
        return ShippingMode.STANDARD;
    }

    @Override
    public Shipping quote(List<OrderItem> items, String zipCode) {
        BigDecimal cost = BASE.add(PER_UNIT.multiply(BigDecimal.valueOf(totalUnits(items))));
        int days = ZipCodes.isSoutheast(zipCode) ? 5 : 9;
        return new Shipping("Entrega economica", cost, days);
    }
}
