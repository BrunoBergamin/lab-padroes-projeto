package br.com.bergamin.patterns.pricing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Decorator.
 *
 * Cada beneficio embrulha o calculo anterior em vez de somar mais um if ao mesmo metodo.
 * A ordem em que voce embrulha muda a conta: cashback sobre um total ja com cupom rende
 * menos que sobre o total cheio, e isso fica explicito na montagem.
 */
public abstract class PriceDecorator implements PriceCalculation {

    protected final PriceCalculation inner;

    protected PriceDecorator(PriceCalculation inner) {
        this.inner = Objects.requireNonNull(inner, "inner nao pode ser nulo");
    }

    @Override
    public BigDecimal total() {
        BigDecimal result = inner.total().subtract(discount());
        return result.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }

    public PriceCalculation inner() {
        return inner;
    }

    public abstract String label();

    public abstract BigDecimal discount();
}
