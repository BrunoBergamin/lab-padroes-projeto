package br.com.bergamin.checkout.events;

import br.com.bergamin.checkout.notification.Notifier;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

/** Ouvinte que avisa o cliente por todos os canais registrados como bean. */
@Component
public class CustomerNoticeListener {

    private final List<Notifier> notifiers;

    public CustomerNoticeListener(List<Notifier> notifiers) {
        this.notifiers = List.copyOf(notifiers);
    }

    @EventListener
    public void on(CheckoutCompletedEvent event) {
        notifiers.forEach(notifier -> notifier.send(event));
    }

    public List<Notifier> notifiers() {
        return notifiers;
    }
}
