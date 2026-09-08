package br.com.bergamin.checkout;

import br.com.bergamin.checkout.order.OrderItem;
import br.com.bergamin.checkout.shipping.ExpressShippingPolicy;
import br.com.bergamin.checkout.shipping.Shipping;
import br.com.bergamin.checkout.shipping.ShippingMode;
import br.com.bergamin.checkout.shipping.ShippingPolicyFactory;
import br.com.bergamin.checkout.shipping.StandardShippingPolicy;
import br.com.bergamin.checkout.shipping.StorePickupPolicy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Strategy + Factory: a modalidade escolhe a politica, sem nenhum if no meio do caminho. */
class ShippingFactoryTest {

    private final ShippingPolicyFactory factory = new ShippingPolicyFactory(
            List.of(new StorePickupPolicy(), new StandardShippingPolicy(), new ExpressShippingPolicy()));

    private final List<OrderItem> items =
            List.of(new OrderItem("TST-01", "Item de teste", new BigDecimal("100.00"), 2));

    @Test
    @DisplayName("retirada na loja nao cobra frete")
    void retiradaNaoCobra() {
        Shipping quote = factory.of(ShippingMode.PICKUP).quote(items, null);

        assertThat(quote.cost()).isEqualByComparingTo("0.00");
        assertThat(quote.days()).isZero();
    }

    @Test
    @DisplayName("economica cobra base mais valor por unidade")
    void economicaCobraPorUnidade() {
        Shipping quote = factory.of(ShippingMode.STANDARD).quote(items, "13010-000");

        assertThat(quote.cost()).isEqualByComparingTo("22.90");
        assertThat(quote.days()).isEqualTo(5);
    }

    @Test
    @DisplayName("expressa e mais cara e chega antes")
    void expressaChegaAntes() {
        Shipping quote = factory.of(ShippingMode.EXPRESS).quote(items, "13010-000");

        assertThat(quote.cost()).isEqualByComparingTo("44.90");
        assertThat(quote.days()).isEqualTo(1);
    }

    @Test
    @DisplayName("CEP fora do sudeste aumenta o prazo, nao o preco")
    void foraDoSudesteDemoraMais() {
        Shipping sudeste = factory.of(ShippingMode.STANDARD).quote(items, "13010-000");
        Shipping norte = factory.of(ShippingMode.STANDARD).quote(items, "69000-000");

        assertThat(norte.cost()).isEqualByComparingTo(sudeste.cost());
        assertThat(norte.days()).isGreaterThan(sudeste.days());
    }

    @Test
    @DisplayName("modalidade sem politica registrada falha rapido")
    void modalidadeSemPolitica() {
        ShippingPolicyFactory vazia = new ShippingPolicyFactory(List.of());

        assertThatThrownBy(() -> vazia.of(ShippingMode.STANDARD))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
