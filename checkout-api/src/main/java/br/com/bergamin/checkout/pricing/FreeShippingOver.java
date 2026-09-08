package br.com.bergamin.checkout.pricing;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class FreeShippingOver extends PriceDecorator {

    private final BigDecimal threshold;
    private final BigDecimal itemsSubtotal;
    private final BigDecimal shippingCost;

    public FreeShippingOver(PriceCalculation inner,
                            BigDecimal threshold,
                            BigDecimal itemsSubtotal,
                            BigDecimal shippingCost) {
        super(inner);
        this.threshold = threshold;
        this.itemsSubtotal = itemsSubtotal;
        this.shippingCost = shippingCost;
    }

    @Override
    public String label() {
        return "Frete gratis acima de R$ " + threshold;
    }

    @Override
    public BigDecimal discount() {
        if (itemsSubtotal.compareTo(threshold) < 0) {
            return BigDecimal.ZERO;
        }
        return shippingCost.setScale(2, RoundingMode.HALF_UP);
    }
}
