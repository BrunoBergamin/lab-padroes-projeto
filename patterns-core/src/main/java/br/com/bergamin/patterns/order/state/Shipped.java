package br.com.bergamin.patterns.order.state;

import br.com.bergamin.patterns.order.OrderState;

/** Estado final: depois que saiu para entrega nao ha transicao de volta. */
public final class Shipped implements OrderState {

    public static final Shipped INSTANCE = new Shipped();

    private Shipped() {
    }

    @Override
    public String name() {
        return "ENVIADO";
    }
}
