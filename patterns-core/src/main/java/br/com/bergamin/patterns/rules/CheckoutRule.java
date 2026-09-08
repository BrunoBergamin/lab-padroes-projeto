package br.com.bergamin.patterns.rules;

/**
 * Chain of Responsibility.
 *
 * Cada regra so conhece a si mesma e o proximo elo. Incluir uma validacao nova nao mexe em
 * nenhuma das existentes: entra na montagem da corrente e pronto.
 */
public abstract class CheckoutRule {

    private CheckoutRule next;

    public static CheckoutRule chainOf(CheckoutRule... rules) {
        if (rules.length == 0) {
            throw new IllegalArgumentException("corrente vazia");
        }
        for (int i = 0; i < rules.length - 1; i++) {
            rules[i].next = rules[i + 1];
        }
        return rules[0];
    }

    public final void check(CheckoutContext context) {
        apply(context);
        if (next != null) {
            next.check(context);
        }
    }

    protected abstract void apply(CheckoutContext context);

    protected void reject(String reason) {
        throw new CheckoutRejectedException(getClass().getSimpleName(), reason);
    }
}
