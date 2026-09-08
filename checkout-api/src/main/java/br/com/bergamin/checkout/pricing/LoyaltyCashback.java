package br.com.bergamin.checkout.pricing;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class LoyaltyCashback extends PriceDecorator {

    private final BigDecimal percentage;
    private final BigDecimal cap;

    public LoyaltyCashback(PriceCalculation inner, BigDecimal percentage, BigDecimal cap) {
        super(inner);
        this.percentage = percentage;
        this.cap = cap;
    }

    @Override
    public String label() {
        return "Cashback fidelidade";
    }

    @Override
    public BigDecimal discount() {
        return inner.total().multiply(percentage).min(cap).setScale(2, RoundingMode.HALF_UP);
    }
}
