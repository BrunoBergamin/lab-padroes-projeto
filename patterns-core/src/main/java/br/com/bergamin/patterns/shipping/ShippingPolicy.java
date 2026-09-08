package br.com.bergamin.patterns.shipping;

import br.com.bergamin.patterns.order.OrderItem;

import java.util.List;

/**
 * Strategy.
 *
 * Cada modalidade de frete cobra por uma conta diferente. Sem o padrao isso vira um switch
 * no meio do calculo do carrinho, e cada transportadora nova reabre o mesmo metodo.
 */
public interface ShippingPolicy {

    ShippingMode mode();

    Shipping quote(List<OrderItem> items, String zipCode);

    default int totalUnits(List<OrderItem> items) {
        return items.stream().mapToInt(OrderItem::quantity).sum();
    }
}
