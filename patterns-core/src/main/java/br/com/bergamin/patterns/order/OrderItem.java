package br.com.bergamin.patterns.order;

import java.math.BigDecimal;
import java.util.Objects;

/** Linha do pedido: o preco fica congelado no momento da compra. */
public record OrderItem(String sku, String name, BigDecimal unitPrice, int quantity) {

    public OrderItem {
        Objects.requireNonNull(sku, "sku nao pode ser nulo");
        Objects.requireNonNull(unitPrice, "unitPrice nao pode ser nulo");
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantidade precisa ser positiva: " + quantity);
        }
    }

    public BigDecimal subtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
