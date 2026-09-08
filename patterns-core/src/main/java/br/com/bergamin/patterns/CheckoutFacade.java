package br.com.bergamin.patterns;

import br.com.bergamin.patterns.catalog.Catalog;
import br.com.bergamin.patterns.catalog.Product;
import br.com.bergamin.patterns.events.CheckoutEvent;
import br.com.bergamin.patterns.events.EventBus;
import br.com.bergamin.patterns.order.Order;
import br.com.bergamin.patterns.order.OrderItem;
import br.com.bergamin.patterns.pricing.BasePrice;
import br.com.bergamin.patterns.pricing.CouponDiscount;
import br.com.bergamin.patterns.pricing.Coupons;
import br.com.bergamin.patterns.pricing.FreeShippingOver;
import br.com.bergamin.patterns.pricing.LoyaltyCashback;
import br.com.bergamin.patterns.pricing.PriceBreakdown;
import br.com.bergamin.patterns.pricing.PriceCalculation;
import br.com.bergamin.patterns.rules.CheckoutContext;
import br.com.bergamin.patterns.rules.CheckoutRejectedException;
import br.com.bergamin.patterns.rules.CheckoutRule;
import br.com.bergamin.patterns.rules.CouponAccepted;
import br.com.bergamin.patterns.rules.DeliverableRegion;
import br.com.bergamin.patterns.rules.MinimumAmount;
import br.com.bergamin.patterns.rules.StockAvailable;
import br.com.bergamin.patterns.shipping.Shipping;
import br.com.bergamin.patterns.shipping.ShippingPolicies;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Facade.
 *
 * Fechar um pedido significa validar, cotar frete, calcular preco, criar o pedido, baixar
 * estoque e avisar quem precisa saber. Quem chama o checkout nao deveria conhecer essa
 * sequencia nem a ordem dela; conhece um metodo.
 */
public final class CheckoutFacade {

    private static final BigDecimal MINIMUM_ORDER = new BigDecimal("50.00");
    private static final BigDecimal FREE_SHIPPING_FROM = new BigDecimal("299.00");
    private static final BigDecimal CASHBACK_RATE = new BigDecimal("0.05");
    private static final BigDecimal CASHBACK_CAP = new BigDecimal("30.00");

    private final Catalog catalog;
    private final EventBus events;
    private final CheckoutRule rules;
    private final Clock clock;

    public CheckoutFacade(Catalog catalog, EventBus events) {
        this(catalog, events, Clock.systemDefaultZone());
    }

    public CheckoutFacade(Catalog catalog, EventBus events, Clock clock) {
        this.catalog = catalog;
        this.events = events;
        this.clock = clock;
        this.rules = CheckoutRule.chainOf(
                new StockAvailable(catalog),
                new MinimumAmount(MINIMUM_ORDER),
                new CouponAccepted(),
                new DeliverableRegion()
        );
    }

    public CheckoutReceipt checkout(CheckoutRequest request) {
        List<OrderItem> items = toItems(request.items());

        CheckoutContext context = new CheckoutContext(
                request.customer(), items, request.zipCode(), request.coupon(), request.mode());
        rules.check(context);

        Shipping shipping = ShippingPolicies.of(request.mode()).quote(items, request.zipCode());
        BigDecimal subtotal = context.subtotal();

        PriceCalculation price = new BasePrice(subtotal, shipping.cost());
        if (Coupons.exists(request.coupon())) {
            price = new CouponDiscount(price, request.coupon());
        }
        price = new FreeShippingOver(price, FREE_SHIPPING_FROM, subtotal, shipping.cost());
        if (request.loyaltyMember()) {
            price = new LoyaltyCashback(price, CASHBACK_RATE, CASHBACK_CAP);
        }

        Order order = Order.builder()
                .customer(request.customer())
                .items(items)
                .zipCode(request.zipCode())
                .shippingLabel(shipping.label())
                .total(price.total())
                .build();

        items.forEach(item -> catalog.reduceStock(item.sku(), item.quantity()));

        events.publish(new CheckoutEvent(order.id(), order.customer(), order.total(),
                order.status(), clock.instant()));

        return new CheckoutReceipt(order, shipping, PriceBreakdown.of(price), order.total());
    }

    private List<OrderItem> toItems(Map<String, Integer> requested) {
        if (requested.isEmpty()) {
            throw new CheckoutRejectedException("Carrinho", "carrinho vazio");
        }
        List<OrderItem> items = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : requested.entrySet()) {
            Product product = catalog.findBySku(entry.getKey())
                    .orElseThrow(() -> new CheckoutRejectedException("Catalogo",
                            "produto fora do catalogo: " + entry.getKey()));
            items.add(new OrderItem(product.sku(), product.name(), product.unitPrice(), entry.getValue()));
        }
        return items;
    }
}
