package br.com.bergamin.patterns;

import br.com.bergamin.patterns.catalog.Catalog;
import br.com.bergamin.patterns.catalog.Product;
import br.com.bergamin.patterns.events.CheckoutEvent;
import br.com.bergamin.patterns.events.EventBus;
import br.com.bergamin.patterns.notification.EmailNotifier;
import br.com.bergamin.patterns.notification.Notifier;
import br.com.bergamin.patterns.notification.SmsNotifier;
import br.com.bergamin.patterns.rules.CheckoutRejectedException;
import br.com.bergamin.patterns.shipping.ShippingMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CheckoutFacadeTest {

    private Catalog catalog;
    private EventBus events;
    private List<CheckoutEvent> published;
    private CheckoutFacade checkout;

    @BeforeEach
    void setUp() {
        catalog = Catalog.getInstance();
        catalog.save(new Product("SER-014", "Serum facial vitamina C", new BigDecimal("129.00"), 12));
        catalog.save(new Product("PIN-007", "Kit de pinceis profissional", new BigDecimal("249.90"), 3));

        published = new ArrayList<>();
        events = new EventBus();
        events.subscribe(published::add);
        checkout = new CheckoutFacade(catalog, events);
    }

    @Test
    @DisplayName("itens, frete expresso, cupom e cashback compoem o total")
    void happyPath() {
        CheckoutReceipt receipt = checkout.checkout(new CheckoutRequest(
                "Ana Flavia", "13010-000", ShippingMode.EXPRESS, "BEMVINDA10", true,
                Map.of("SER-014", 2)));

        assertEquals(new BigDecimal("44.90"), receipt.shipping().cost());
        assertEquals(new BigDecimal("258.98"), receipt.total());
        assertEquals("AGUARDANDO_PAGAMENTO", receipt.order().status());
    }

    @Test
    @DisplayName("checkout aprovado baixa o estoque")
    void reducesStock() {
        checkout.checkout(new CheckoutRequest("Ana Flavia", "13010-000", ShippingMode.PICKUP, null, false,
                Map.of("SER-014", 2)));

        assertEquals(10, catalog.findBySku("SER-014").orElseThrow().stock());
    }

    @Test
    @DisplayName("checkout barrado nao encosta no estoque nem publica evento")
    void rejectedKeepsStock() {
        assertThrows(CheckoutRejectedException.class, () -> checkout.checkout(new CheckoutRequest(
                "Bruno", "13010-000", ShippingMode.STANDARD, null, false, Map.of("PIN-007", 5))));

        assertEquals(3, catalog.findBySku("PIN-007").orElseThrow().stock());
        assertTrue(published.isEmpty());
    }

    @Test
    @DisplayName("o evento chega aos ouvintes com o total ja fechado")
    void publishesEvent() {
        CheckoutReceipt receipt = checkout.checkout(new CheckoutRequest(
                "Ana Flavia", "13010-000", ShippingMode.PICKUP, null, false, Map.of("SER-014", 1)));

        assertEquals(1, published.size());
        assertEquals(receipt.order().id(), published.get(0).orderId());
        assertEquals(receipt.total(), published.get(0).total());
    }

    @Test
    @DisplayName("SMS so sai acima de R$ 200; e-mail sai sempre")
    void notifierHook() {
        Notifier email = new EmailNotifier();
        Notifier sms = new SmsNotifier();
        events.subscribe(event -> {
            email.send(event);
            sms.send(event);
        });

        checkout.checkout(new CheckoutRequest("Ana Flavia", "13010-000", ShippingMode.PICKUP, null, false,
                Map.of("SER-014", 1)));

        assertEquals(1, email.outbox().size());
        assertTrue(sms.outbox().isEmpty());
    }

    @Test
    @DisplayName("ouvinte que estoura nao derruba o checkout")
    void listenerFailureIsIsolated() {
        events.subscribe(event -> {
            throw new IllegalStateException("integracao fora do ar");
        });

        CheckoutReceipt receipt = checkout.checkout(new CheckoutRequest(
                "Ana Flavia", "13010-000", ShippingMode.PICKUP, null, false, Map.of("SER-014", 1)));

        assertEquals(new BigDecimal("129.00"), receipt.total());
        assertEquals(1, events.failures().size());
    }
}
