package br.com.bergamin.patterns.rules;

import br.com.bergamin.patterns.catalog.Catalog;
import br.com.bergamin.patterns.catalog.Product;
import br.com.bergamin.patterns.order.OrderItem;

/** Barra o pedido se algum item nao tem estoque suficiente. */
public final class StockAvailable extends CheckoutRule {

    private final Catalog catalog;

    public StockAvailable(Catalog catalog) {
        this.catalog = catalog;
    }

    @Override
    protected void apply(CheckoutContext context) {
        for (OrderItem item : context.items()) {
            Product product = catalog.findBySku(item.sku())
                    .orElseThrow(() -> new CheckoutRejectedException("StockAvailable",
                            "produto fora do catalogo: " + item.sku()));
            if (product.stock() < item.quantity()) {
                reject("estoque insuficiente para " + product.name()
                        + ": pedido " + item.quantity() + ", disponivel " + product.stock());
            }
        }
    }
}
