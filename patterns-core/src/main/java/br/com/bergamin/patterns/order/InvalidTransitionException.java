package br.com.bergamin.patterns.order;

/** Transicao que o estado atual do pedido nao permite. */
public class InvalidTransitionException extends IllegalStateException {

    public InvalidTransitionException(String currentState, String action) {
        super("pedido em " + currentState + " nao pode " + action);
    }
}
