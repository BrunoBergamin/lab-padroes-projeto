package br.com.bergamin.patterns.notification;

import br.com.bergamin.patterns.events.CheckoutEvent;

/** E-mail vai sempre, com o texto completo. */
public final class EmailNotifier extends Notifier {

    @Override
    protected String channel() {
        return "email";
    }

    @Override
    protected String body(CheckoutEvent event) {
        return "Ola " + event.customer() + ", recebemos seu pedido no valor de R$ "
                + event.total() + ". Status atual: " + event.status() + ".";
    }

    @Override
    protected void deliver(String message) {
        System.out.println("  -> " + message);
    }
}
