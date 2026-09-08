package br.com.bergamin.checkout;

import br.com.bergamin.checkout.catalog.CachedProductQuery;
import br.com.bergamin.checkout.catalog.Product;
import br.com.bergamin.checkout.catalog.ProductQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/** Proxy: mesma interface do objeto real, controlando quantas vezes ele e chamado. */
class CatalogProxyTest {

    /** Objeto real de mentira, so para contar os acessos. */
    private static class CountingQuery implements ProductQuery {

        private final AtomicInteger calls = new AtomicInteger();

        @Override
        public Optional<Product> findBySku(String sku) {
            calls.incrementAndGet();
            return Optional.of(new Product(sku, "Item de teste", new BigDecimal("100.00"), 10));
        }

        @Override
        public List<Product> all() {
            return List.of();
        }
    }

    @Test
    @DisplayName("segunda consulta ao mesmo sku nao chega no objeto real")
    void segundaConsultaVemDoCache() {
        CountingQuery real = new CountingQuery();
        CachedProductQuery proxy = new CachedProductQuery(real);

        proxy.findBySku("TST-01");
        proxy.findBySku("TST-01");

        assertThat(real.calls.get()).isEqualTo(1);
        assertThat(proxy.hits()).isEqualTo(1);
        assertThat(proxy.misses()).isEqualTo(1);
    }

    @Test
    @DisplayName("depois do evict o proxy volta a consultar o objeto real")
    void evictForcaNovaConsulta() {
        CountingQuery real = new CountingQuery();
        CachedProductQuery proxy = new CachedProductQuery(real);

        proxy.findBySku("TST-01");
        proxy.evict("TST-01");
        proxy.findBySku("TST-01");

        assertThat(real.calls.get()).isEqualTo(2);
    }
}
