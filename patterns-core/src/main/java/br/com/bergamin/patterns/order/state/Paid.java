package br.com.bergamin.patterns.order.state;

import br.com.bergamin.patterns.order.OrderState;

public final class Paid implements OrderState {

    public static final Paid INSTANCE = new Paid();

    private Paid() {
    }

    @Override
    public String name() {
        return "PAGO";
    }

    @Override
    public OrderState ship() {
        return Shipped.INSTANCE;
    }

    @Override
    public OrderState cancel() {
        return Cancelled.INSTANCE;
    }
}
