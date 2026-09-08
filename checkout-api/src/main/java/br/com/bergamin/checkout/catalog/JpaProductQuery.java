package br.com.bergamin.checkout.catalog;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/** O objeto real do Proxy: quem de fato conversa com o banco. */
@Component("jpaProductQuery")
public class JpaProductQuery implements ProductQuery {

    private final ProductRepository repository;

    public JpaProductQuery(ProductRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Product> findBySku(String sku) {
        return repository.findById(sku);
    }

    @Override
    public List<Product> all() {
        return repository.findAll();
    }
}
