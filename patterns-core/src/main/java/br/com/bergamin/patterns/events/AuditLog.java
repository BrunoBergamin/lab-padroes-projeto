package br.com.bergamin.patterns.events;

import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** Ouvinte de auditoria: registra o que aconteceu, sem opinar sobre o pedido. */
public final class AuditLog implements CheckoutListener {

    private static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").withZone(ZoneId.systemDefault());

    private final List<String> entries = new CopyOnWriteArrayList<>();

    @Override
    public void onCheckout(CheckoutEvent event) {
        entries.add(FORMAT.format(event.occurredAt()) + " pedido=" + event.orderId()
                + " cliente=" + event.customer() + " total=" + event.total());
    }

    public List<String> entries() {
        return List.copyOf(entries);
    }
}
