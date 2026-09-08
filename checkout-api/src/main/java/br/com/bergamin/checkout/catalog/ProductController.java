package br.com.bergamin.checkout.catalog;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/produtos")
@Tag(name = "Catalogo")
public class ProductController {

    private final ProductRepository products;
    private final ProductQuery query;
    private final CachedProductQuery cache;

    public ProductController(ProductRepository products, ProductQuery query, CachedProductQuery cache) {
        this.products = products;
        this.query = query;
        this.cache = cache;
    }

    public record ProductView(String sku, String nome, BigDecimal preco, int estoque) {
        static ProductView of(Product product) {
            return new ProductView(product.getSku(), product.getName(), product.getUnitPrice(), product.getStock());
        }
    }

    public record NewProduct(@NotBlank String sku,
                             @NotBlank String nome,
                             @NotNull BigDecimal preco,
                             @PositiveOrZero int estoque) {
    }

    @GetMapping
    @Operation(summary = "Lista o catalogo")
    public List<ProductView> list() {
        return query.all().stream().map(ProductView::of).toList();
    }

    @PostMapping
    @Operation(summary = "Cadastra um produto")
    public ResponseEntity<ProductView> create(@Valid @RequestBody NewProduct body) {
        Product saved = products.save(new Product(body.sku(), body.nome(), body.preco(), body.estoque()));
        cache.evict(saved.getSku());
        return ResponseEntity.status(HttpStatus.CREATED).body(ProductView.of(saved));
    }
}
