package br.com.bergamin.patterns.pricing;

import java.math.BigDecimal;

/** Componente do Decorator: qualquer coisa que saiba dizer quanto o cliente paga. */
public interface PriceCalculation {

    BigDecimal total();
}
