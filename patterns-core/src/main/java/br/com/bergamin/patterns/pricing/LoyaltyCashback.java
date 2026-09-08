package br.com.bergamin.patterns.pricing;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Abate o cashback do programa de fidelidade, respeitando um teto. */
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
        BigDecimal raw = inner.total().multiply(percentage);
        return raw.min(cap).setScale(2, RoundingMode.HALF_UP);
    }
}
