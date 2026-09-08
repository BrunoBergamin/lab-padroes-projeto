package br.com.bergamin.checkout.rules;

import br.com.bergamin.checkout.catalog.Product;
import br.com.bergamin.checkout.catalog.ProductQuery;
import br.com.bergamin.checkout.order.OrderItem;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/** Primeiro elo: sem estoque nao adianta calcular mais nada. */
@Component
@Order(10)
public class StockAvailableRule implements CheckoutRule {

    private final ProductQuery products;

    public StockAvailableRule(ProductQuery products) {
        this.products = products;
    }

    @Override
    public void apply(CheckoutContext context) {
        for (OrderItem item : context.items()) {
            Product product = products.findBySku(item.getSku())
                    .orElseThrow(() -> new CheckoutRejectedException("StockAvailableRule",
                            "produto fora do catalogo: " + item.getSku()));
            if (product.getStock() < item.getQuantity()) {
                reject("estoque insuficiente para " + product.getName()
                        + ": pedido " + item.getQuantity() + ", disponivel " + product.getStock());
            }
        }
    }
}
