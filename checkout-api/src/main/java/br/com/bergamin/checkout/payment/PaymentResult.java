package br.com.bergamin.checkout.payment;

/** Resposta do gateway ja no vocabulario da aplicacao. */
public record PaymentResult(boolean approved, String authorizationCode, String reason) {

    public static PaymentResult approved(String authorizationCode) {
        return new PaymentResult(true, authorizationCode, null);
    }

    public static PaymentResult declined(String reason) {
        return new PaymentResult(false, null, reason);
    }
}
