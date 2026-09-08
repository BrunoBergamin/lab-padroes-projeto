package br.com.bergamin.checkout.order;

/** Transicao que o status atual do pedido nao permite. */
public class InvalidTransitionException extends IllegalStateException {

    public InvalidTransitionException(String currentStatus, String action) {
        super("pedido em " + currentStatus + " nao pode " + action);
    }
}
