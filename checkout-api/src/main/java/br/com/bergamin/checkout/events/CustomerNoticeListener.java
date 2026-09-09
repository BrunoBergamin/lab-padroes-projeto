package br.com.bergamin.checkout.events;

import br.com.bergamin.checkout.notification.Notifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Ouvinte que avisa o cliente por todos os canais registrados como bean.
 *
 * Duas decisoes aqui, e as duas sao a mesma ideia do EventBus do modulo Java puro:
 *
 * 1. AFTER_COMMIT: avisar o cliente de um pedido que ainda pode dar rollback seria mandar
 *    e-mail de pedido que nao existe. O aviso so sai depois que o banco confirmou.
 * 2. try/catch por canal: o SMS cair nao pode derrubar o e-mail nem o pedido. Aviso e
 *    efeito colateral, nao parte da transacao. (Como isso roda depois do commit, uma
 *    excecao aqui nao desfaz o pedido, mas ainda assim subiria ate quem chamou.)
 */
@Component
public class CustomerNoticeListener {

    private static final Logger log = LoggerFactory.getLogger(CustomerNoticeListener.class);

    private final List<Notifier> notifiers;
    private final List<String> failures = new CopyOnWriteArrayList<>();

    public CustomerNoticeListener(List<Notifier> notifiers) {
        this.notifiers = List.copyOf(notifiers);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(CheckoutCompletedEvent event) {
        for (Notifier notifier : notifiers) {
            try {
                notifier.send(event);
            } catch (RuntimeException e) {
                String canal = notifier.getClass().getSimpleName();
                failures.add(canal + ": " + e.getMessage());
                log.warn("falha ao avisar o cliente pelo canal {} no pedido {}",
                        canal, event.orderId(), e);
            }
        }
    }

    public List<Notifier> notifiers() {
        return notifiers;
    }

    public List<String> failures() {
        return List.copyOf(failures);
    }
}
