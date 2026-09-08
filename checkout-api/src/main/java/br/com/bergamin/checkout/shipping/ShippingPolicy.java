package br.com.bergamin.checkout.shipping;

import br.com.bergamin.checkout.order.OrderItem;

import java.util.List;

/**
 * Strategy.
 *
 * Cada implementacao e um @Component. O Spring injeta todas juntas e a fabrica escolhe pela
 * modalidade; nenhum ponto do codigo precisa de switch para cotar frete.
 */
public interface ShippingPolicy {

    ShippingMode mode();

    Shipping quote(List<OrderItem> items, String zipCode);

    default int totalUnits(List<OrderItem> items) {
        return items.stream().mapToInt(OrderItem::getQuantity).sum();
    }
}
