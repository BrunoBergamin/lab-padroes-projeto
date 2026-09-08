package br.com.bergamin.patterns;

import br.com.bergamin.patterns.catalog.Catalog;
import br.com.bergamin.patterns.catalog.Product;
import br.com.bergamin.patterns.order.OrderItem;
import br.com.bergamin.patterns.rules.CheckoutContext;
import br.com.bergamin.patterns.rules.CheckoutRejectedException;
import br.com.bergamin.patterns.rules.CheckoutRule;
import br.com.bergamin.patterns.rules.CouponAccepted;
import br.com.bergamin.patterns.rules.DeliverableRegion;
import br.com.bergamin.patterns.rules.MinimumAmount;
import br.com.bergamin.patterns.rules.StockAvailable;
import br.com.bergamin.patterns.shipping.ShippingMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CheckoutRuleChainTest {

    private CheckoutRule chain;

    @BeforeEach
    void setUp() {
        Catalog catalog = Catalog.getInstance();
        catalog.save(new Product("SER-014", "Serum facial vitamina C", new BigDecimal("129.00"), 12));
        catalog.save(new Product("PIN-007", "Kit de pinceis profissional", new BigDecimal("249.90"), 3));
        catalog.save(new Product("BAT-001", "Batom matte vermelho", new BigDecimal("39.90"), 40));

        chain = CheckoutRule.chainOf(
                new StockAvailable(catalog),
                new MinimumAmount(new BigDecimal("50.00")),
                new CouponAccepted(),
                new DeliverableRegion());
    }

    private CheckoutContext context(String sku, BigDecimal price, int quantity,
                                    String zipCode, String coupon, ShippingMode mode) {
        List<OrderItem> items = List.of(new OrderItem(sku, sku, price, quantity));
        return new CheckoutContext("Ana Flavia", items, zipCode, coupon, mode);
    }

    @Test
    @DisplayName("carrinho valido atravessa a corrente inteira")
    void accepted() {
        assertDoesNotThrow(() -> chain.check(context(
                "SER-014", new BigDecimal("129.00"), 1, "13010-000", "BEMVINDA10", ShippingMode.STANDARD)));
    }

    @Test
    @DisplayName("estoque insuficiente para na primeira regra")
    void outOfStock() {
        CheckoutRejectedException error = assertThrows(CheckoutRejectedException.class, () -> chain.check(context(
                "PIN-007", new BigDecimal("249.90"), 5, "13010-000", null, ShippingMode.STANDARD)));

        assertEquals("StockAvailable", error.rule());
    }

    @Test
    @DisplayName("abaixo do pedido minimo")
    void belowMinimum() {
        CheckoutRejectedException error = assertThrows(CheckoutRejectedException.class, () -> chain.check(context(
                "BAT-001", new BigDecimal("39.90"), 1, "13010-000", null, ShippingMode.STANDARD)));

        assertEquals("MinimumAmount", error.rule());
    }

    @Test
    @DisplayName("cupom inexistente")
    void unknownCoupon() {
        CheckoutRejectedException error = assertThrows(CheckoutRejectedException.class, () -> chain.check(context(
                "SER-014", new BigDecimal("129.00"), 1, "13010-000", "NAOEXISTE", ShippingMode.STANDARD)));

        assertEquals("CouponAccepted", error.rule());
    }

    @Test
    @DisplayName("regiao ainda nao atendida")
    void blockedRegion() {
        CheckoutRejectedException error = assertThrows(CheckoutRejectedException.class, () -> chain.check(context(
                "SER-014", new BigDecimal("129.00"), 1, "69900-000", null, ShippingMode.STANDARD)));

        assertEquals("DeliverableRegion", error.rule());
    }

    @Test
    @DisplayName("retirada na loja dispensa a checagem de CEP")
    void pickupSkipsZipCode() {
        assertDoesNotThrow(() -> chain.check(context(
                "SER-014", new BigDecimal("129.00"), 1, "", null, ShippingMode.PICKUP)));
    }
}
