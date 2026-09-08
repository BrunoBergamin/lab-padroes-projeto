package br.com.bergamin.patterns;

import br.com.bergamin.patterns.pricing.BasePrice;
import br.com.bergamin.patterns.pricing.CouponDiscount;
import br.com.bergamin.patterns.pricing.FreeShippingOver;
import br.com.bergamin.patterns.pricing.LoyaltyCashback;
import br.com.bergamin.patterns.pricing.PriceBreakdown;
import br.com.bergamin.patterns.pricing.PriceCalculation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PriceDecoratorTest {

    private static final BigDecimal ITEMS = new BigDecimal("100.00");
    private static final BigDecimal SHIPPING = new BigDecimal("20.00");

    @Test
    @DisplayName("preco base soma itens e frete")
    void basePrice() {
        assertEquals(new BigDecimal("120.00"), new BasePrice(ITEMS, SHIPPING).total());
    }

    @Test
    @DisplayName("os beneficios se empilham sem que um conheca o outro")
    void stacked() {
        PriceCalculation price = new BasePrice(ITEMS, SHIPPING);
        price = new CouponDiscount(price, "BLACK25");
        price = new FreeShippingOver(price, new BigDecimal("100.00"), ITEMS, SHIPPING);
        price = new LoyaltyCashback(price, new BigDecimal("0.05"), new BigDecimal("2.00"));

        assertEquals(new BigDecimal("68.00"), price.total());
    }

    @Test
    @DisplayName("cashback com teto faz a ordem dos decorators mudar o total")
    void orderMatters() {
        PriceCalculation cashbackFirst = new CouponDiscount(
                new LoyaltyCashback(new BasePrice(ITEMS, SHIPPING), new BigDecimal("0.05"), new BigDecimal("5.00")),
                "BLACK25");
        PriceCalculation couponFirst = new LoyaltyCashback(
                new CouponDiscount(new BasePrice(ITEMS, SHIPPING), "BLACK25"),
                new BigDecimal("0.05"), new BigDecimal("5.00"));

        assertEquals(new BigDecimal("86.25"), cashbackFirst.total());
        assertEquals(new BigDecimal("85.50"), couponFirst.total());
    }

    @Test
    @DisplayName("frete gratis nao vale abaixo do limite")
    void belowThreshold() {
        PriceCalculation price = new FreeShippingOver(
                new BasePrice(ITEMS, SHIPPING), new BigDecimal("300.00"), ITEMS, SHIPPING);

        assertEquals(new BigDecimal("120.00"), price.total());
    }

    @Test
    @DisplayName("o extrato lista base, frete e cada abatimento aplicado")
    void breakdown() {
        PriceCalculation price = new CouponDiscount(new BasePrice(ITEMS, SHIPPING), "BLACK25");

        List<PriceBreakdown.Line> lines = PriceBreakdown.of(price);

        assertEquals(3, lines.size());
        assertEquals("Itens", lines.get(0).label());
        assertEquals("Frete", lines.get(1).label());
        assertEquals(new BigDecimal("-30.00"), lines.get(2).amount());
    }
}
