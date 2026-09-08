package br.com.bergamin.checkout.events;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Observer, versao Spring.
 *
 * No Java puro existia um EventBus escrito na mao. Aqui o barramento e o proprio contexto:
 * o servico publica com ApplicationEventPublisher e o Spring entrega a quem anotou
 * @EventListener. Ouvinte novo nao mexe no checkout.
 */
@Component
public class AuditLogListener {

    private static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").withZone(ZoneId.systemDefault());

    private final List<String> entries = new CopyOnWriteArrayList<>();

    @EventListener
    public void on(CheckoutCompletedEvent event) {
        entries.add(FORMAT.format(event.occurredAt()) + " pedido=" + event.orderId()
                + " cliente=" + event.customer() + " total=" + event.total());
    }

    public List<String> entries() {
        return List.copyOf(entries);
    }
}
