package br.com.bergamin.patterns.notification;

import br.com.bergamin.patterns.events.CheckoutEvent;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Template Method.
 *
 * O roteiro do aviso e sempre o mesmo: decidir se vale enviar, montar assunto, montar corpo,
 * entregar. O que muda por canal sao as etapas, nao a ordem delas.
 */
public abstract class Notifier {

    private final List<String> outbox = new CopyOnWriteArrayList<>();

    public final void send(CheckoutEvent event) {
        if (!shouldSend(event)) {
            return;
        }
        String message = channel() + " | " + subject(event) + " | " + body(event);
        outbox.add(message);
        deliver(message);
    }

    protected boolean shouldSend(CheckoutEvent event) {
        return true;
    }

    protected String subject(CheckoutEvent event) {
        return "Pedido " + event.orderId().substring(0, 8) + " confirmado";
    }

    protected abstract String channel();

    protected abstract String body(CheckoutEvent event);

    protected abstract void deliver(String message);

    public List<String> outbox() {
        return List.copyOf(outbox);
    }
}
