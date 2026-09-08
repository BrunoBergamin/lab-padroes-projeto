package br.com.bergamin.patterns.shipping;

import java.math.BigDecimal;

/** Cotacao de frete: rotulo que o cliente le, custo e prazo. */
public record Shipping(String label, BigDecimal cost, int days) {
}
