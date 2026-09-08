package br.com.bergamin.checkout.payment;

import org.springframework.stereotype.Component;

/**
 * O sistema legado do banco, que ninguem pode alterar.
 *
 * Fala em centavos, devolve string com codigo e usa nomes que so fazem sentido la dentro.
 * Esta classe existe para o Adapter ter o que adaptar.
 */
@Component
public class LegacyBankApi {

    /** Retorna "OK;<nsu>" ou "ERR;<motivo>". */
    public String executeTransaction(String payerDocument, long amountInCents, String currencyCode) {
        if (amountInCents <= 0) {
            return "ERR;valor invalido";
        }
        if (payerDocument == null || payerDocument.isBlank()) {
            return "ERR;pagador nao identificado";
        }
        if (!"BRL".equals(currencyCode)) {
            return "ERR;moeda nao suportada";
        }
        return "OK;" + Math.abs((payerDocument + amountInCents).hashCode());
    }
}
