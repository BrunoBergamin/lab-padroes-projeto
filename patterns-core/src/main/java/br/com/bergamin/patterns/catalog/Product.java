package br.com.bergamin.patterns.catalog;

import java.math.BigDecimal;
import java.util.Objects;

/** Produto do catalogo. Preco unitario em BRL. */
public record Product(String sku, String name, BigDecimal unitPrice, int stock) {

    public Product {
        Objects.requireNonNull(sku, "sku nao pode ser nulo");
        Objects.requireNonNull(name, "name nao pode ser nulo");
        Objects.requireNonNull(unitPrice, "unitPrice nao pode ser nulo");
        if (unitPrice.signum() < 0) {
            throw new IllegalArgumentException("preco negativo: " + unitPrice);
        }
        if (stock < 0) {
            throw new IllegalArgumentException("estoque negativo: " + stock);
        }
    }

    public Product withStock(int newStock) {
        return new Product(sku, name, unitPrice, newStock);
    }
}
