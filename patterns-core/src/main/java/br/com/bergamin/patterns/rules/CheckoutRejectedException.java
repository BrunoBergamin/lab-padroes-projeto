package br.com.bergamin.patterns.rules;

/** Checkout barrado por uma regra da corrente. */
public class CheckoutRejectedException extends RuntimeException {

    private final String rule;

    public CheckoutRejectedException(String rule, String reason) {
        super(reason);
        this.rule = rule;
    }

    public String rule() {
        return rule;
    }
}
