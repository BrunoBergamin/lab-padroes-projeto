package br.com.bergamin.patterns.order.state;

import br.com.bergamin.patterns.order.OrderState;

public final class AwaitingPayment implements OrderState {

    public static final AwaitingPayment INSTANCE = new AwaitingPayment();

    private AwaitingPayment() {
    }

    @Override
    public String name() {
        return "AGUARDANDO_PAGAMENTO";
    }

    @Override
    public OrderState pay() {
        return Paid.INSTANCE;
    }

    @Override
    public OrderState cancel() {
        return Cancelled.INSTANCE;
    }
}
