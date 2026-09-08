package br.com.bergamin.patterns;

import br.com.bergamin.patterns.order.Order;
import br.com.bergamin.patterns.pricing.PriceBreakdown;
import br.com.bergamin.patterns.shipping.Shipping;

import java.math.BigDecimal;
import java.util.List;

/** Resultado do checkout: o pedido criado e o extrato de como o total foi formado. */
public record CheckoutReceipt(Order order,
                              Shipping shipping,
                              List<PriceBreakdown.Line> breakdown,
                              BigDecimal total) {
}
