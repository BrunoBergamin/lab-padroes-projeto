package br.com.bergamin.checkout.pricing;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class CouponDiscount extends PriceDecorator {

    private final String code;
    private final BigDecimal percentage;

    public CouponDiscount(PriceCalculation inner, String code, BigDecimal percentage) {
        super(inner);
        this.code = code == null ? "" : code.toUpperCase();
        this.percentage = percentage;
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
