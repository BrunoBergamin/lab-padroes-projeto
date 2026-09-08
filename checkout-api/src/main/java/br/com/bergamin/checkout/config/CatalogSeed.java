package br.com.bergamin.checkout.config;

import br.com.bergamin.checkout.catalog.Product;
import br.com.bergamin.checkout.catalog.ProductRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;

/** Catalogo de exemplo para o H2 subir com algo dentro. */
@Configuration
public class CatalogSeed {

    @Bean
    public ApplicationRunner seedCatalog(ProductRepository products) {
        return args -> {
            if (products.count() > 0) {
                return;
            }
            products.saveAll(List.of(
                    new Product("BAT-001", "Batom matte vermelho", new BigDecimal("39.90"), 40),
                    new Product("SER-014", "Serum facial vitamina C", new BigDecimal("129.00"), 12),
                    new Product("PIN-007", "Kit de pinceis profissional", new BigDecimal("249.90"), 3),
                    new Product("PER-021", "Perfume floral 100ml", new BigDecimal("319.00"), 0)
            ));
        };
    }
}
