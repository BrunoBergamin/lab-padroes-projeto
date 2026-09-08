package br.com.bergamin.patterns;

import br.com.bergamin.patterns.order.OrderItem;
import br.com.bergamin.patterns.shipping.Shipping;
import br.com.bergamin.patterns.shipping.ShippingMode;
import br.com.bergamin.patterns.shipping.ShippingPolicies;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShippingPolicyTest {

    private final List<OrderItem> items = List.of(
            new OrderItem("SER-014", "Serum", new BigDecimal("129.00"), 2));

    @Test
    @DisplayName("retirada na loja nao cobra frete")
    void pickup() {
        Shipping quote = ShippingPolicies.of(ShippingMode.PICKUP).quote(items, "13010-000");

        assertEquals(0, quote.cost().signum());
        assertEquals(0, quote.days());
    }

    @Test
    @DisplayName("economica cobra base mais valor por unidade")
    void standard() {
        Shipping quote = ShippingPolicies.of(ShippingMode.STANDARD).quote(items, "13010-000");

        assertEquals(new BigDecimal("22.90"), quote.cost());
        assertEquals(5, quote.days());
    }

    @Test
    @DisplayName("expressa custa mais e chega antes")
    void express() {
        Shipping standard = ShippingPolicies.of(ShippingMode.STANDARD).quote(items, "13010-000");
        Shipping express = ShippingPolicies.of(ShippingMode.EXPRESS).quote(items, "13010-000");

        assertTrue(express.cost().compareTo(standard.cost()) > 0);
        assertTrue(express.days() < standard.days());
    }

    @Test
    @DisplayName("fora do Sudeste o prazo aumenta")
    void farAway() {
        Shipping quote = ShippingPolicies.of(ShippingMode.STANDARD).quote(items, "66000-000");

        assertEquals(9, quote.days());
    }
}
