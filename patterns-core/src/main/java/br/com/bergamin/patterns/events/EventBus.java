package br.com.bergamin.patterns.events;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Observer (o Subject).
 *
 * O checkout publica e segue a vida. Um ouvinte que estoura nao derruba os outros nem o
 * pedido: e efeito colateral, nao parte da transacao.
 */
public final class EventBus {

    private final List<CheckoutListener> listeners = new CopyOnWriteArrayList<>();
    private final List<String> failures = new CopyOnWriteArrayList<>();

    public void subscribe(CheckoutListener listener) {
        listeners.add(listener);
    }

    public void publish(CheckoutEvent event) {
        for (CheckoutListener listener : listeners) {
            try {
                listener.onCheckout(event);
            } catch (RuntimeException e) {
                failures.add(listener.getClass().getSimpleName() + ": " + e.getMessage());
            }
        }
    }

    public List<String> failures() {
        return List.copyOf(failures);
    }

    public int listenerCount() {
        return listeners.size();
    }
}
