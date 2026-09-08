package br.com.bergamin.checkout.payment;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Adapter: converte reais em centavos, e a string do legado em PaymentResult. */
@Component
public class LegacyBankPaymentAdapter implements PaymentGateway {

    private final LegacyBankApi legacy;

    public LegacyBankPaymentAdapter(LegacyBankApi legacy) {
        this.legacy = legacy;
    }

    @Override
    public PaymentResult authorize(String customer, BigDecimal amount) {
        long cents = amount.setScale(2, RoundingMode.HALF_UP).movePointRight(2).longValueExact();
        String response = legacy.executeTransaction(customer, cents, "BRL");
        String[] parts = response.split(";", 2);
        return "OK".equals(parts[0])
                ? PaymentResult.approved(parts[1])
                : PaymentResult.declined(parts.length > 1 ? parts[1] : "recusado pelo banco");
    }
}
