package br.com.bergamin.checkout.rules;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/** Pedido abaixo do minimo nao fecha. O valor vem do application.yml. */
@Component
@Order(20)
public class MinimumAmountRule implements CheckoutRule {

    private final BigDecimal minimum;

    public MinimumAmountRule(@Value("${checkout.pedido-minimo}") BigDecimal minimum) {
        this.minimum = minimum;
    }

    @Override
    public void apply(CheckoutContext context) {
        if (context.subtotal().compareTo(minimum) < 0) {
            reject("pedido minimo de R$ " + minimum + ", carrinho em R$ " + context.subtotal());
        }
    }
}
