package br.com.bergamin.patterns.events;

/** Observer: reage ao checkout sem que o checkout saiba quem esta ouvindo. */
public interface CheckoutListener {

    void onCheckout(CheckoutEvent event);
}
