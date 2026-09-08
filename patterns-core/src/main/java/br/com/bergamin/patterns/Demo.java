package br.com.bergamin.patterns;

import br.com.bergamin.patterns.catalog.Catalog;
import br.com.bergamin.patterns.catalog.Product;
import br.com.bergamin.patterns.events.AuditLog;
import br.com.bergamin.patterns.events.CustomerNotice;
import br.com.bergamin.patterns.events.EventBus;
import br.com.bergamin.patterns.notification.EmailNotifier;
import br.com.bergamin.patterns.notification.SmsNotifier;
import br.com.bergamin.patterns.order.InvalidTransitionException;
import br.com.bergamin.patterns.order.Order;
import br.com.bergamin.patterns.pricing.PriceBreakdown;
import br.com.bergamin.patterns.rules.CheckoutRejectedException;
import br.com.bergamin.patterns.shipping.ShippingMode;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Roteiro de demonstracao: o motor de checkout rodando sem nenhum framework. */
public final class Demo {

    private static final Locale BR = Locale.of("pt", "BR");

    public static void main(String[] args) {
        Catalog catalog = Catalog.getInstance();
        AuditLog audit = new AuditLog();

        EventBus events = new EventBus();
        events.subscribe(audit);
        events.subscribe(new CustomerNotice(List.of(new EmailNotifier(), new SmsNotifier())));

        CheckoutFacade checkout = new CheckoutFacade(catalog, events);

        title("Catalogo (Singleton)");
        for (Product product : catalog.all()) {
            System.out.printf("  %-8s %-32s %10s  estoque %d%n",
                    product.sku(), product.name(), money(product.unitPrice()), product.stock());
        }

        title("Checkout aprovado");
        CheckoutReceipt receipt = checkout.checkout(new CheckoutRequest(
                "Ana Flavia", "13010-000", ShippingMode.EXPRESS, "BEMVINDA10", true,
                Map.of("SER-014", 2)));
        print(receipt);

        title("Ciclo de vida do pedido (State)");
        Order order = receipt.order();
        System.out.println("  status inicial: " + order.status());
        order.pay();
        System.out.println("  apos pagar:     " + order.status());
        order.ship();
        System.out.println("  apos enviar:    " + order.status());
        try {
            order.cancel();
        } catch (InvalidTransitionException e) {
            System.out.println("  recusado:       " + e.getMessage());
        }

        title("Checkout barrado (Chain of Responsibility)");
        reject(checkout, new CheckoutRequest("Bruno", "13010-000", ShippingMode.STANDARD, null, false,
                Map.of("PIN-007", 5)));
        reject(checkout, new CheckoutRequest("Bruno", "13010-000", ShippingMode.STANDARD, null, false,
                Map.of("BAT-001", 1)));
        reject(checkout, new CheckoutRequest("Bruno", "69900-000", ShippingMode.STANDARD, null, false,
                Map.of("SER-014", 1)));
        reject(checkout, new CheckoutRequest("Bruno", "13010-000", ShippingMode.STANDARD, "NAOEXISTE", false,
                Map.of("SER-014", 1)));

        title("Auditoria (Observer)");
        audit.entries().forEach(entry -> System.out.println("  " + entry));
    }

    private static void reject(CheckoutFacade checkout, CheckoutRequest request) {
        try {
            checkout.checkout(request);
            System.out.println("  passou (nao deveria)");
        } catch (CheckoutRejectedException e) {
            System.out.printf("  %-18s %s%n", e.rule(), e.getMessage());
        }
    }

    private static void print(CheckoutReceipt receipt) {
        System.out.println("  pedido " + receipt.order().id());
        System.out.println("  frete: " + receipt.shipping().label()
                + " em " + receipt.shipping().days() + " dia(s)");
        for (PriceBreakdown.Line line : receipt.breakdown()) {
            System.out.printf("  %-32s %10s%n", line.label(), money(line.amount()));
        }
        System.out.printf("  %-32s %10s%n", "TOTAL", money(receipt.total()));
    }

    private static String money(BigDecimal value) {
        return String.format(BR, "R$ %,.2f", value);
    }

    private static void title(String text) {
        System.out.println();
        System.out.println("== " + text);
    }
}
