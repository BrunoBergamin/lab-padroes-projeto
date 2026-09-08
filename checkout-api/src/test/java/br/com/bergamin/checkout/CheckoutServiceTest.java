package br.com.bergamin.checkout;

import br.com.bergamin.checkout.catalog.CachedProductQuery;
import br.com.bergamin.checkout.catalog.Product;
import br.com.bergamin.checkout.catalog.ProductRepository;
import br.com.bergamin.checkout.checkout.CheckoutRequest;
import br.com.bergamin.checkout.checkout.CheckoutResponse;
import br.com.bergamin.checkout.checkout.CheckoutService;
import br.com.bergamin.checkout.events.AuditLogListener;
import br.com.bergamin.checkout.order.OrderRepository;
import br.com.bergamin.checkout.rules.CheckoutRejectedException;
import br.com.bergamin.checkout.shipping.ShippingMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * A fachada por dentro: cada teste aqui mostra um padrao trabalhando.
 *
 * Os produtos sao recriados a cada teste para o resultado nao depender da ordem de execucao.
 */
@SpringBootTest
class CheckoutServiceTest {

    private static final String CEP_SUDESTE = "13010-000";

    @Autowired
    private CheckoutService checkout;

    @Autowired
    private ProductRepository products;

    @Autowired
    private OrderRepository orders;

    @Autowired
    private CachedProductQuery cache;

    @Autowired
    private AuditLogListener audit;

    @BeforeEach
    void seed() {
        orders.deleteAll();
        products.deleteAll();
        products.saveAll(List.of(
                new Product("TST-01", "Item caro", new BigDecimal("100.00"), 10),
                new Product("TST-02", "Item barato", new BigDecimal("25.00"), 2)
        ));
        cache.evictAll();
    }

    private CheckoutRequest pedido(Map<String, Integer> itens, String cupom, boolean fidelidade) {
        return new CheckoutRequest("Bruno", CEP_SUDESTE, ShippingMode.STANDARD, cupom, fidelidade, itens);
    }

    @Test
    @DisplayName("Facade: um metodo valida, cota frete, calcula preco e cria o pedido")
    void fechaPedido() {
        CheckoutResponse resposta = checkout.checkout(pedido(Map.of("TST-01", 2), null, false));

        // 200,00 de itens + 22,90 de frete economico
        assertThat(resposta.total()).isEqualByComparingTo("222.90");
        assertThat(resposta.status()).isEqualTo("Aguardando pagamento");
        assertThat(resposta.entrega()).isEqualTo("Entrega economica");
        assertThat(orders.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Decorator: o cupom entra como uma camada e aparece no extrato")
    void aplicaCupom() {
        CheckoutResponse resposta = checkout.checkout(pedido(Map.of("TST-01", 2), "BEMVINDA10", false));

        // 222,90 menos 10% = 200,61
        assertThat(resposta.total()).isEqualByComparingTo("200.61");
        assertThat(resposta.extrato()).anyMatch(linha -> linha.descricao().startsWith("Cupom"));
    }

    @Test
    @DisplayName("Decorator: acima do limite o frete some do total")
    void freteGratisAcimaDoLimite() {
        CheckoutResponse resposta = checkout.checkout(pedido(Map.of("TST-01", 3), null, false));

        // 300,00 de itens + 24,40 de frete - 24,40 de frete gratis
        assertThat(resposta.total()).isEqualByComparingTo("300.00");
        assertThat(resposta.extrato()).anyMatch(linha -> linha.descricao().startsWith("Frete gratis"));
    }

    @Test
    @DisplayName("Decorator: o cashback do fidelidade e a ultima camada")
    void aplicaCashback() {
        CheckoutResponse resposta = checkout.checkout(pedido(Map.of("TST-01", 2), null, true));

        // 222,90 menos 5% = 211,75
        assertThat(resposta.total()).isEqualByComparingTo("211.75");
    }

    @Test
    @DisplayName("Chain of Responsibility: carrinho abaixo do minimo nao passa")
    void barraPedidoAbaixoDoMinimo() {
        assertThatThrownBy(() -> checkout.checkout(pedido(Map.of("TST-02", 1), null, false)))
                .isInstanceOf(CheckoutRejectedException.class)
                .hasMessageContaining("pedido minimo");
    }

    @Test
    @DisplayName("Chain of Responsibility: sem estoque o pedido para na primeira regra")
    void barraSemEstoque() {
        assertThatThrownBy(() -> checkout.checkout(pedido(Map.of("TST-02", 5), null, false)))
                .isInstanceOf(CheckoutRejectedException.class)
                .hasMessageContaining("estoque insuficiente");
    }

    @Test
    @DisplayName("Chain of Responsibility: cupom inexistente derruba o checkout")
    void barraCupomInvalido() {
        assertThatThrownBy(() -> checkout.checkout(pedido(Map.of("TST-01", 2), "NAOEXISTE", false)))
                .isInstanceOf(CheckoutRejectedException.class)
                .hasMessageContaining("cupom invalido");
    }

    @Test
    @DisplayName("Chain of Responsibility: entrega exige CEP valido, retirada nao")
    void cepSoImportaNaEntrega() {
        assertThatThrownBy(() -> checkout.checkout(
                new CheckoutRequest("Bruno", "123", ShippingMode.STANDARD, null, false, Map.of("TST-01", 2))))
                .isInstanceOf(CheckoutRejectedException.class)
                .hasMessageContaining("CEP invalido");

        CheckoutResponse retirada = checkout.checkout(
                new CheckoutRequest("Bruno", null, ShippingMode.PICKUP, null, false, Map.of("TST-01", 2)));

        assertThat(retirada.total()).isEqualByComparingTo("200.00");
    }

    @Test
    @DisplayName("o checkout baixa o estoque e limpa o cache do proxy")
    void baixaEstoque() {
        checkout.checkout(pedido(Map.of("TST-01", 2), null, false));

        assertThat(products.findById("TST-01").orElseThrow().getStock()).isEqualTo(8);
    }

    @Test
    @DisplayName("Observer: o checkout publica o evento e a auditoria registra sozinha")
    void publicaEvento() {
        int antes = audit.entries().size();

        CheckoutResponse resposta = checkout.checkout(pedido(Map.of("TST-01", 2), null, false));

        assertThat(audit.entries()).hasSize(antes + 1);
        assertThat(audit.entries().getLast()).contains(resposta.pedido().toString());
    }

    @Test
    @DisplayName("produto fora do catalogo nem chega nas regras")
    void produtoInexistente() {
        assertThatThrownBy(() -> checkout.checkout(pedido(Map.of("NAO-EXISTE", 1), null, false)))
                .isInstanceOf(CheckoutRejectedException.class)
                .hasMessageContaining("fora do catalogo");
    }
}
