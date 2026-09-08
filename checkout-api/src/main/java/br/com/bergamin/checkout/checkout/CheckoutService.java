package br.com.bergamin.checkout.checkout;

import br.com.bergamin.checkout.catalog.CachedProductQuery;
import br.com.bergamin.checkout.catalog.Product;
import br.com.bergamin.checkout.catalog.ProductQuery;
import br.com.bergamin.checkout.catalog.ProductRepository;
import br.com.bergamin.checkout.events.CheckoutCompletedEvent;
import br.com.bergamin.checkout.order.Order;
import br.com.bergamin.checkout.order.OrderItem;
import br.com.bergamin.checkout.order.OrderRepository;
import br.com.bergamin.checkout.payment.PaymentGateway;
import br.com.bergamin.checkout.payment.PaymentResult;
import br.com.bergamin.checkout.pricing.PriceAssembler;
import br.com.bergamin.checkout.pricing.PriceBreakdown;
import br.com.bergamin.checkout.pricing.PriceCalculation;
import br.com.bergamin.checkout.rules.CheckoutContext;
import br.com.bergamin.checkout.rules.CheckoutRejectedException;
import br.com.bergamin.checkout.rules.CheckoutRuleChain;
import br.com.bergamin.checkout.shipping.Shipping;
import br.com.bergamin.checkout.shipping.ShippingPolicyFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Facade.
 *
 * Fechar um pedido significa validar, cotar frete, calcular preco, criar o pedido, baixar
 * estoque e avisar quem precisa saber. O controller nao conhece essa sequencia nem a ordem
 * dela; conhece um metodo. Cada etapa continua morando no seu proprio padrao.
 */
@Service
public class CheckoutService {

    private final ProductQuery products;
    private final ProductRepository productRepository;
    private final CachedProductQuery cache;
    private final OrderRepository orders;
    private final CheckoutRuleChain rules;
    private final ShippingPolicyFactory shippingPolicies;
    private final PriceAssembler prices;
    private final PaymentGateway payments;
    private final ApplicationEventPublisher events;
    private final Clock clock;

    public CheckoutService(ProductQuery products,
                           ProductRepository productRepository,
                           CachedProductQuery cache,
                           OrderRepository orders,
                           CheckoutRuleChain rules,
                           ShippingPolicyFactory shippingPolicies,
                           PriceAssembler prices,
                           PaymentGateway payments,
                           ApplicationEventPublisher events,
                           Clock clock) {
        this.products = products;
        this.productRepository = productRepository;
        this.cache = cache;
        this.orders = orders;
        this.rules = rules;
        this.shippingPolicies = shippingPolicies;
        this.prices = prices;
        this.payments = payments;
        this.events = events;
        this.clock = clock;
    }

    @Transactional
    public CheckoutResponse checkout(CheckoutRequest request) {
        List<OrderItem> items = toItems(request.itens());

        CheckoutContext context = new CheckoutContext(
                request.cliente(), items, request.cep(), request.cupom(), request.entrega());
        rules.check(context);

        Shipping shipping = shippingPolicies.of(request.entrega()).quote(items, request.cep());
        BigDecimal subtotal = context.subtotal();

        PriceCalculation price = prices.assemble(subtotal, shipping.cost(), request.cupom(), request.fidelidade());

        Order order = orders.save(Order.builder()
                .customer(request.cliente())
                .items(items)
                .zipCode(request.cep())
                .shipping(shipping.label(), shipping.cost())
                .total(price.total())
                .build());

        reduceStock(items);

        events.publishEvent(new CheckoutCompletedEvent(order.getId(), order.getCustomer(),
                order.getTotal(), order.getStatus(), clock.instant()));

        return CheckoutResponse.of(order, shipping, PriceBreakdown.of(price));
    }

    @Transactional(readOnly = true)
    public List<OrderView> orders() {
        return orders.findAll().stream().map(OrderView::of).toList();
    }

    @Transactional(readOnly = true)
    public OrderView order(UUID id) {
        return OrderView.of(find(id));
    }

    /** Adapter na pratica: o servico so conhece PaymentGateway, nunca a API do banco legado. */
    @Transactional
    public OrderView pay(UUID id) {
        Order order = find(id);
        PaymentResult result = payments.authorize(order.getCustomer(), order.getTotal());
        if (!result.approved()) {
            throw new PaymentDeclinedException(result.reason());
        }
        order.pay();
        return OrderView.of(orders.save(order));
    }

    @Transactional
    public OrderView ship(UUID id) {
        Order order = find(id);
        order.ship();
        return OrderView.of(orders.save(order));
    }

    @Transactional
    public OrderView cancel(UUID id) {
        Order order = find(id);
        order.cancel();
        return OrderView.of(orders.save(order));
    }

    private Order find(UUID id) {
        return orders.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
    }

    private List<OrderItem> toItems(Map<String, Integer> requested) {
        if (requested == null || requested.isEmpty()) {
            throw new CheckoutRejectedException("Carrinho", "carrinho vazio");
        }
        List<OrderItem> items = new ArrayList<>();
        requested.forEach((sku, quantity) -> {
            if (quantity == null || quantity <= 0) {
                throw new CheckoutRejectedException("Carrinho", "quantidade invalida para " + sku);
            }
            Product product = products.findBySku(sku)
                    .orElseThrow(() -> new CheckoutRejectedException("Catalogo",
                            "produto fora do catalogo: " + sku));
            items.add(new OrderItem(product.getSku(), product.getName(), product.getUnitPrice(), quantity));
        });
        return items;
    }

    private void reduceStock(List<OrderItem> items) {
        items.forEach(item -> {
            Product product = productRepository.findById(item.getSku()).orElseThrow();
            product.reduceStock(item.getQuantity());
            productRepository.save(product);
            cache.evict(item.getSku());
        });
    }
}
