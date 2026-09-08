package br.com.bergamin.checkout.checkout;

/** Pagamento recusado pelo gateway. Vira 402 no ApiExceptionHandler. */
public class PaymentDeclinedException extends RuntimeException {

    public PaymentDeclinedException(String reason) {
        super(reason);
    }
}
