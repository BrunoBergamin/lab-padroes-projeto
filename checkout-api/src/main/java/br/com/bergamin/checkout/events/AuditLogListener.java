package br.com.bergamin.checkout.events;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Observer, versao Spring.
 *
 * No Java puro existia um EventBus escrito na mao. Aqui o barramento e o proprio contexto:
 * o servico publica com ApplicationEventPublisher e o Spring entrega a quem se inscreveu.
 * Ouvinte novo nao mexe no checkout.
 *
 * AFTER_COMMIT e o detalhe que importa: o @EventListener comum roda dentro da transacao do
 * checkout, entao auditar um pedido que ainda pode dar rollback registraria um pedido que
 * nunca existiu. Esperando o commit, so entra no log o que de fato foi gravado.
 */
@Component
public class AuditLogListener {

    private static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").withZone(ZoneId.systemDefault());

    private final List<String> entries = new CopyOnWriteArrayList<>();

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(CheckoutCompletedEvent event) {
        entries.add(FORMAT.format(event.occurredAt()) + " pedido=" + event.orderId()
                + " cliente=" + event.customer() + " total=" + event.total());
    }

    public List<String> entries() {
        return List.copyOf(entries);
    }
}
