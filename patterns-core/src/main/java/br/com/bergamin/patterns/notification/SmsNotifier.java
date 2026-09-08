package br.com.bergamin.patterns.notification;

import br.com.bergamin.patterns.events.CheckoutEvent;

import java.math.BigDecimal;

/** SMS custa por mensagem: so sai em pedido acima de R$ 200. */
public final class SmsNotifier extends Notifier {

    private static final BigDecimal THRESHOLD = new BigDecimal("200.00");

    @Override
    protected boolean shouldSend(CheckoutEvent event) {
        return event.total().compareTo(THRESHOLD) >= 0;
    }

    @Override
    protected String channel() {
        return "sms";
    }

    @Override
    protected String body(CheckoutEvent event) {
        return "Pedido confirmado: R$ " + event.total();
    }

    @Override
    protected void deliver(String message) {
        System.out.println("  -> " + message);
    }
}
