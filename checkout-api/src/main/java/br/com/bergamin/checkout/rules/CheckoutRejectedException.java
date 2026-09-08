package br.com.bergamin.checkout.rules;

/** Checkout barrado por uma regra da corrente. Vira 422 no ApiExceptionHandler. */
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
