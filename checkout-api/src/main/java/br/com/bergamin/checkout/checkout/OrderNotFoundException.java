package br.com.bergamin.checkout.checkout;

import java.util.UUID;

/** Pedido inexistente. Vira 404 no ApiExceptionHandler. */
public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(UUID id) {
        super("pedido nao encontrado: " + id);
    }
}
