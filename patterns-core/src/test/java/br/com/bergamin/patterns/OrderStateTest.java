package br.com.bergamin.patterns;

import br.com.bergamin.patterns.order.InvalidTransitionException;
import br.com.bergamin.patterns.order.Order;
import br.com.bergamin.patterns.order.OrderItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderStateTest {

    private Order newOrder() {
        return Order.builder()
                .customer("Ana Flavia")
                .zipCode("13010-000")
                .shippingLabel("Entrega expressa")
                .item(new OrderItem("SER-014", "Serum", new BigDecimal("129.00"), 1))
                .total(new BigDecimal("129.00"))
                .build();
    }

    @Test
    @DisplayName("caminho feliz: aguardando pagamento, pago, enviado")
    void happyPath() {
        Order order = newOrder();
        assertEquals("AGUARDANDO_PAGAMENTO", order.status());

        order.pay();
        assertEquals("PAGO", order.status());

        order.ship();
        assertEquals("ENVIADO", order.status());
    }

    @Test
    @DisplayName("pedido enviado nao volta atras")
    void shippedIsFinal() {
        Order order = newOrder();
        order.pay();
        order.ship();

        assertThrows(InvalidTransitionException.class, order::cancel);
        assertThrows(InvalidTransitionException.class, order::pay);
    }

    @Test
    @DisplayName("nao se envia o que nao foi pago")
    void cannotShipUnpaid() {
        InvalidTransitionException error = assertThrows(InvalidTransitionException.class, newOrder()::ship);

        assertEquals("pedido em AGUARDANDO_PAGAMENTO nao pode ser enviado", error.getMessage());
    }

    @Test
    @DisplayName("builder exige os campos obrigatorios")
    void builderValidates() {
        assertThrows(NullPointerException.class, () -> Order.builder()
                .item(new OrderItem("SER-014", "Serum", new BigDecimal("129.00"), 1))
                .build());
    }
}
