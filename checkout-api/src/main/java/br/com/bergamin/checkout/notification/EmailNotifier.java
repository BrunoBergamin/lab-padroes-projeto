package br.com.bergamin.checkout.notification;

import br.com.bergamin.checkout.events.CheckoutCompletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** E-mail vai sempre, com o texto completo. */
@Component
public class EmailNotifier extends Notifier {

    private static final Logger log = LoggerFactory.getLogger(EmailNotifier.class);

    @Override
    protected String channel() {
        return "email";
    }

    @Override
    protected String body(CheckoutCompletedEvent event) {
        return "Ola " + event.customer() + ", recebemos seu pedido no valor de R$ "
                + event.total() + ". Status atual: " + event.status().label() + ".";
    }

    @Override
    protected void deliver(String message) {
        log.info("{}", message);
    }
}
