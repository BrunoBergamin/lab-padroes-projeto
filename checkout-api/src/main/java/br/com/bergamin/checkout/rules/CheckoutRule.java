package br.com.bergamin.checkout.rules;

/**
 * Chain of Responsibility, na versao Spring.
 *
 * No Java puro cada regra guardava a referencia do proximo elo. Aqui nenhuma regra conhece
 * a seguinte: cada uma e um @Component com @Order, o Spring injeta a lista ja ordenada e o
 * CheckoutRuleChain percorre. Regra nova = classe nova, nenhuma linha alterada.
 */
public interface CheckoutRule {

    void apply(CheckoutContext context);

    default void reject(String reason) {
        throw new CheckoutRejectedException(getClass().getSimpleName(), reason);
    }
}
