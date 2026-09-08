package br.com.bergamin.patterns.rules;

import java.math.BigDecimal;

/** Pedido abaixo do minimo nao fecha. */
public final class MinimumAmount extends CheckoutRule {

    private final BigDecimal minimum;

    public MinimumAmount(BigDecimal minimum) {
        this.minimum = minimum;
    }

    @Override
    protected void apply(CheckoutContext context) {
        if (context.subtotal().compareTo(minimum) < 0) {
            reject("pedido minimo de R$ " + minimum + ", carrinho em R$ " + context.subtotal());
        }
    }
}
