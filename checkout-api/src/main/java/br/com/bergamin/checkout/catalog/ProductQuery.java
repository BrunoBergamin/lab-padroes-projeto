package br.com.bergamin.checkout.catalog;

import java.util.List;
import java.util.Optional;

/** Contrato de leitura do catalogo. O proxy e o repositorio real implementam o mesmo tipo. */
public interface ProductQuery {

    Optional<Product> findBySku(String sku);

    List<Product> all();
}
