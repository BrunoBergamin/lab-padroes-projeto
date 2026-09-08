package br.com.bergamin.patterns.events;

import br.com.bergamin.patterns.notification.Notifier;

import java.util.List;

/** Ouvinte que avisa o cliente pelos canais configurados. */
public final class CustomerNotice implements CheckoutListener {

    private final List<Notifier> notifiers;

    public CustomerNotice(List<Notifier> notifiers) {
        this.notifiers = List.copyOf(notifiers);
    }

    @Override
    public void onCheckout(CheckoutEvent event) {
        notifiers.forEach(notifier -> notifier.send(event));
    }
}
