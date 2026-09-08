package br.com.bergamin.checkout;

import br.com.bergamin.checkout.payment.LegacyBankApi;
import br.com.bergamin.checkout.payment.LegacyBankPaymentAdapter;
import br.com.bergamin.checkout.payment.PaymentResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/** Adapter: reais viram centavos e a string do legado vira um resultado tipado. */
class PaymentAdapterTest {

    private final LegacyBankPaymentAdapter adapter = new LegacyBankPaymentAdapter(new LegacyBankApi());

    @Test
    @DisplayName("pagamento valido volta aprovado com codigo de autorizacao")
    void aprovado() {
        PaymentResult result = adapter.authorize("Bruno", new BigDecimal("222.90"));

        assertThat(result.approved()).isTrue();
        assertThat(result.authorizationCode()).isNotBlank();
    }

    @Test
    @DisplayName("valor zerado volta recusado com o motivo do banco")
    void recusado() {
        PaymentResult result = adapter.authorize("Bruno", BigDecimal.ZERO);

        assertThat(result.approved()).isFalse();
        assertThat(result.reason()).isEqualTo("valor invalido");
    }

    @Test
    @DisplayName("cliente vazio volta recusado")
    void semPagador() {
        PaymentResult result = adapter.authorize("  ", new BigDecimal("10.00"));

        assertThat(result.approved()).isFalse();
    }
}
