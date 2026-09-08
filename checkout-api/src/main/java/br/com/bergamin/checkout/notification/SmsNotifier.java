package br.com.bergamin.checkout.notification;

import br.com.bergamin.checkout.events.CheckoutCompletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/** SMS custa por mensagem: so sai em pedido acima de R$ 200. */
@Component
public class SmsNotifier extends Notifier {

    private static final Logger log = LoggerFactory.getLogger(SmsNotifier.class);
    private static final BigDecimal THRESHOLD = new BigDecimal("200.00");

    @Override
    protected boolean shouldSend(CheckoutCompletedEvent event) {
        return event.total().compareTo(THRESHOLD) >= 0;
    }

    @Override
    protected String channel() {
        return "sms";
    }

    @Override
    protected String body(CheckoutCompletedEvent event) {
        return "Pedido confirmado: R$ " + event.total();
    }

    @Override
    protected void deliver(String message) {
        log.info("{}", message);
    }
}
