package br.com.bergamin.patterns.pricing;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** O preco cru: itens mais frete, sem nenhum beneficio aplicado. */
public final class BasePrice implements PriceCalculation {

    private final BigDecimal itemsSubtotal;
    private final BigDecimal shippingCost;

    public BasePrice(BigDecimal itemsSubtotal, BigDecimal shippingCost) {
        this.itemsSubtotal = itemsSubtotal;
        this.shippingCost = shippingCost;
    }

    @Override
    public BigDecimal total() {
        return itemsSubtotal.add(shippingCost).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal itemsSubtotal() {
        return itemsSubtotal.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal shippingCost() {
        return shippingCost.setScale(2, RoundingMode.HALF_UP);
    }
}
