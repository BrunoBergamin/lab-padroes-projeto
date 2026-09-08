package br.com.bergamin.patterns.catalog;

import java.math.BigDecimal;
import java.util.List;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton (initialization-on-demand holder).
 *
 * A instancia so nasce quando alguem chama getInstance(), e a JVM garante que isso acontece
 * uma vez so, sem synchronized no caminho de leitura.
 */
public final class Catalog {

    private final Map<String, Product> products = new ConcurrentHashMap<>();

    private Catalog() {
        seed();
    }

    private static final class Holder {
        private static final Catalog INSTANCE = new Catalog();
    }

    public static Catalog getInstance() {
        return Holder.INSTANCE;
    }

    private void seed() {
        save(new Product("BAT-001", "Batom matte vermelho", new BigDecimal("39.90"), 40));
        save(new Product("SER-014", "Serum facial vitamina C", new BigDecimal("129.00"), 12));
        save(new Product("PIN-007", "Kit de pinceis profissional", new BigDecimal("249.90"), 3));
        save(new Product("PER-021", "Perfume floral 100ml", new BigDecimal("319.00"), 0));
    }

    public void save(Product product) {
        products.put(product.sku(), product);
    }

    public Optional<Product> findBySku(String sku) {
        return Optional.ofNullable(products.get(sku));
    }

    public List<Product> all() {
        return products.values().stream()
                .sorted(Comparator.comparing(Product::sku))
                .toList();
    }

    public void reduceStock(String sku, int quantity) {
        products.computeIfPresent(sku, (key, product) -> product.withStock(product.stock() - quantity));
    }
}
