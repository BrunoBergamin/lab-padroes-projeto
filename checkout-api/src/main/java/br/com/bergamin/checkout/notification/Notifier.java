package br.com.bergamin.checkout.notification;

import br.com.bergamin.checkout.events.CheckoutCompletedEvent;

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

    public final void send(CheckoutCompletedEvent event) {
        if (!shouldSend(event)) {
            return;
        }
        String message = channel() + " | " + subject(event) + " | " + body(event);
        outbox.add(message);
        deliver(message);
    }

    protected boolean shouldSend(CheckoutCompletedEvent event) {
        return true;
    }

    protected String subject(CheckoutCompletedEvent event) {
        return "Pedido " + event.orderId().toString().substring(0, 8) + " confirmado";
    }

    protected abstract String channel();

    protected abstract String body(CheckoutCompletedEvent event);

    protected abstract void deliver(String message);

    public List<String> outbox() {
        return List.copyOf(outbox);
    }
}
