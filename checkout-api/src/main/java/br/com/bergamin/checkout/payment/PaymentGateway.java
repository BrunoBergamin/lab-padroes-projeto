package br.com.bergamin.checkout.payment;

import java.math.BigDecimal;

/**
 * Adapter (o alvo).
 *
 * A aplicacao fala BigDecimal, nome de cliente e resultado tipado. Quem traduz isso para o
 * dialeto do banco legado e o adaptador; trocar de banco significa escrever outro adaptador.
 */
public interface PaymentGateway {

    PaymentResult authorize(String customer, BigDecimal amount);
}
