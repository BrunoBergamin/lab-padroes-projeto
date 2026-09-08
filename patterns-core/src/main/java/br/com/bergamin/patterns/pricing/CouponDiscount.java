package br.com.bergamin.patterns.pricing;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Aplica o percentual do cupom sobre o total acumulado ate aqui. */
public final class CouponDiscount extends PriceDecorator {

    private final String code;
    private final BigDecimal percentage;

    public CouponDiscount(PriceCalculation inner, String code) {
        super(inner);
        this.code = code == null ? "" : code.toUpperCase();
        this.percentage = Coupons.percentageOf(code).orElse(BigDecimal.ZERO);
    }

    @Override
    public String label() {
        return "Cupom " + code;
    }

    @Override
    public BigDecimal discount() {
        return inner.total().multiply(percentage).setScale(2, RoundingMode.HALF_UP);
    }
}
