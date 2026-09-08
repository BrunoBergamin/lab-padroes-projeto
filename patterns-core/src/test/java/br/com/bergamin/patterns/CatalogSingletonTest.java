package br.com.bergamin.patterns;

import br.com.bergamin.patterns.catalog.Catalog;
import br.com.bergamin.patterns.catalog.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CatalogSingletonTest {

    @BeforeEach
    void resetStock() {
        Catalog.getInstance().save(new Product("SER-014", "Serum facial vitamina C", new BigDecimal("129.00"), 12));
    }

    @Test
    @DisplayName("getInstance devolve sempre a mesma instancia")
    void singleInstance() {
        assertSame(Catalog.getInstance(), Catalog.getInstance());
    }

    @Test
    @DisplayName("baixa de estoque enxerga a mesma instancia de qualquer ponto do codigo")
    void sharedState() {
        Catalog.getInstance().reduceStock("SER-014", 2);

        int stock = Catalog.getInstance().findBySku("SER-014").orElseThrow().stock();

        assertEquals(10, stock);
    }

    @Test
    @DisplayName("sku desconhecido devolve Optional vazio em vez de null")
    void unknownSku() {
        assertTrue(Catalog.getInstance().findBySku("NAO-EXISTE").isEmpty());
    }
}
