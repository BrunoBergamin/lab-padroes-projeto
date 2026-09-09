package br.com.bergamin.checkout;

import br.com.bergamin.checkout.catalog.CachedProductQuery;
import br.com.bergamin.checkout.catalog.Product;
import br.com.bergamin.checkout.catalog.ProductRepository;
import br.com.bergamin.checkout.checkout.CheckoutRequest;
import br.com.bergamin.checkout.checkout.CheckoutResponse;
import br.com.bergamin.checkout.checkout.CheckoutService;
import br.com.bergamin.checkout.events.CheckoutCompletedEvent;
import br.com.bergamin.checkout.events.CustomerNoticeListener;
import br.com.bergamin.checkout.notification.Notifier;
import br.com.bergamin.checkout.order.OrderRepository;
import br.com.bergamin.checkout.shipping.ShippingMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Observer: avisar o cliente e efeito colateral, nao parte do pedido.
 *
 * Aqui entra um canal que sempre quebra, para provar que o pedido fecha do mesmo jeito.
 */
@SpringBootTest
@Import(NoticeFailureTest.CanalQuebrado.class)
class NoticeFailureTest {

    @TestConfiguration
    static class CanalQuebrado {

        @Bean
        Notifier canalQuebrado() {
            return new Notifier() {

                @Override
                protected String channel() {
                    return "canal-quebrado";
                }

                @Override
                protected String body(CheckoutCompletedEvent event) {
                    return "nunca chega";
                }

                @Override
                protected void deliver(String message) {
                    throw new IllegalStateException("provedor fora do ar");
                }
            };
        }
    }

    @Autowired
    private CheckoutService checkout;

    @Autowired
    private CustomerNoticeListener notices;

    @Autowired
    private ProductRepository products;

    @Autowired
    private OrderRepository orders;

    @Autowired
    private CachedProductQuery cache;

    @BeforeEach
    void seed() {
        orders.deleteAll();
        products.deleteAll();
        products.save(new Product("TST-01", "Item caro", new BigDecimal("100.00"), 10));
        cache.evictAll();
    }

    @Test
    @DisplayName("canal de aviso quebrado nao derruba o pedido nem os outros canais")
    void avisoQuebradoNaoDerrubaOPedido() {
        CheckoutResponse resposta = checkout.checkout(new CheckoutRequest(
                "Bruno", "13010-000", ShippingMode.STANDARD, null, false, Map.of("TST-01", 2)));

        assertThat(resposta.total()).isEqualByComparingTo("222.90");
        assertThat(orders.count()).isEqualTo(1);
        assertThat(notices.failures()).anyMatch(falha -> falha.contains("provedor fora do ar"));

        List<String> canais = notices.notifiers().stream()
                .map(notifier -> notifier.getClass().getSimpleName())
                .toList();
        assertThat(canais).hasSize(3);
    }
}
