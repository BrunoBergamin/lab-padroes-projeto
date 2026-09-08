package br.com.bergamin.checkout;

import br.com.bergamin.checkout.order.InvalidTransitionException;
import br.com.bergamin.checkout.order.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** State: quem decide o que pode virar o que sao os proprios estados. */
class OrderStatusTest {

    @Test
    @DisplayName("aguardando pagamento vira pago e depois enviado")
    void caminhoFeliz() {
        assertThat(OrderStatus.AWAITING_PAYMENT.pay()).isEqualTo(OrderStatus.PAID);
        assertThat(OrderStatus.PAID.ship()).isEqualTo(OrderStatus.SHIPPED);
    }

    @Test
    @DisplayName("nao da para enviar antes de pagar")
    void enviarAntesDePagar() {
        assertThatThrownBy(OrderStatus.AWAITING_PAYMENT::ship)
                .isInstanceOf(InvalidTransitionException.class);
    }

    @Test
    @DisplayName("pedido enviado nao pode ser cancelado")
    void cancelarEnviado() {
        assertThatThrownBy(OrderStatus.SHIPPED::cancel)
                .isInstanceOf(InvalidTransitionException.class);
    }

    @Test
    @DisplayName("pedido cancelado e ponto final")
    void canceladoNaoVolta() {
        assertThatThrownBy(OrderStatus.CANCELLED::pay)
                .isInstanceOf(InvalidTransitionException.class);
    }
}
