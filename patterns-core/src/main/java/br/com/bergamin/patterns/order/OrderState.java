package br.com.bergamin.patterns.order;

/**
 * State.
 *
 * Cada estado responde por si. Em vez de um if/else dentro do pedido perguntando "qual e o
 * status?", cada classe implementa so as transicoes que fazem sentido nela; as outras caem
 * no default e recusam.
 */
public interface OrderState {

    String name();

    default OrderState pay() {
        throw new InvalidTransitionException(name(), "ser pago");
    }

    default OrderState ship() {
        throw new InvalidTransitionException(name(), "ser enviado");
    }

    default OrderState cancel() {
        throw new InvalidTransitionException(name(), "ser cancelado");
    }
}
