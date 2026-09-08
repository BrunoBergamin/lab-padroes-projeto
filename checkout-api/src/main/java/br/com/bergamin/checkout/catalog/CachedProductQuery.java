package br.com.bergamin.checkout.catalog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Proxy.
 *
 * Mesma interface do objeto real, controlando o acesso a ele: guarda o produto em memoria e
 * so vai ao banco quando nao tem. Quem depende de ProductQuery nao muda uma linha, porque a
 * @Primary aqui faz o Spring injetar o proxy no lugar do real.
 */
@Component
@Primary
public class CachedProductQuery implements ProductQuery {

    private static final Logger log = LoggerFactory.getLogger(CachedProductQuery.class);

    private final ProductQuery target;
    private final Map<String, Product> cache = new ConcurrentHashMap<>();
    private final AtomicInteger hits = new AtomicInteger();
    private final AtomicInteger misses = new AtomicInteger();

    public CachedProductQuery(@Qualifier("jpaProductQuery") ProductQuery target) {
        this.target = target;
    }

    @Override
    public Optional<Product> findBySku(String sku) {
        Product cached = cache.get(sku);
        if (cached != null) {
            hits.incrementAndGet();
            log.debug("cache hit para o sku {}", sku);
            return Optional.of(cached);
        }
        misses.incrementAndGet();
        Optional<Product> found = target.findBySku(sku);
        found.ifPresent(product -> cache.put(sku, product));
        return found;
    }

    @Override
    public List<Product> all() {
        return target.all();
    }

    /** Estoque mudou, preco mudou, produto novo: o proxy precisa esquecer o que sabia. */
    public void evict(String sku) {
        cache.remove(sku);
    }

    public void evictAll() {
        cache.clear();
    }

    public int hits() {
        return hits.get();
    }

    public int misses() {
        return misses.get();
    }
}
