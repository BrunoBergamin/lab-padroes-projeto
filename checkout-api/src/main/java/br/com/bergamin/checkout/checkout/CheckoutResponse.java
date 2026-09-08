package br.com.bergamin.checkout.checkout;

import br.com.bergamin.checkout.order.Order;
import br.com.bergamin.checkout.order.OrderItem;
import br.com.bergamin.checkout.pricing.PriceBreakdown;
import br.com.bergamin.checkout.shipping.Shipping;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/** Resultado do checkout: o pedido criado e o extrato de como o total foi formado. */
public record CheckoutResponse(UUID pedido,
                               String cliente,
                               String status,
                               List<Item> itens,
                               String entrega,
                               int prazoEmDias,
                               List<PriceBreakdown.Line> extrato,
                               BigDecimal total) {

    public record Item(String sku, String nome, int quantidade, BigDecimal subtotal) {
        static Item of(OrderItem item) {
            return new Item(item.getSku(), item.getName(), item.getQuantity(), item.subtotal());
        }
    }

    public static CheckoutResponse of(Order order, Shipping shipping, List<PriceBreakdown.Line> breakdown) {
        return new CheckoutResponse(
                order.getId(),
                order.getCustomer(),
                order.getStatus().label(),
                order.getItems().stream().map(Item::of).toList(),
                shipping.label(),
                shipping.days(),
                breakdown,
                order.getTotal());
    }
}
