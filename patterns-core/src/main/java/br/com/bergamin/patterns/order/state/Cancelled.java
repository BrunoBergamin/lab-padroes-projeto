package br.com.bergamin.patterns.order.state;

import br.com.bergamin.patterns.order.OrderState;

/** Estado final. */
public final class Cancelled implements OrderState {

    public static final Cancelled INSTANCE = new Cancelled();

    private Cancelled() {
    }

    @Override
    public String name() {
        return "CANCELADO";
    }
}
