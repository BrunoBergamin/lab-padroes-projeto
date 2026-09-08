package br.com.bergamin.checkout.patterns;

import br.com.bergamin.checkout.catalog.CachedProductQuery;
import br.com.bergamin.checkout.events.CustomerNoticeListener;
import br.com.bergamin.checkout.rules.CheckoutRuleChain;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Endpoint de vitrine: mostra os padroes aplicados e o que esta montado em tempo de execucao.
 *
 * Serve de indice para quem abre o projeto pela primeira vez e quer saber onde cada padrao mora.
 */
@RestController
@RequestMapping("/api/padroes")
@Tag(name = "Padroes")
public class PatternsController {

    private static final List<Pattern> CATALOG = List.of(
            new Pattern("Singleton", "Catalog (Java puro) e o escopo singleton dos beans no Spring",
                    "catalog"),
            new Pattern("Builder", "Order.builder() monta o pedido com campos nomeados",
                    "order/Order.java"),
            new Pattern("Factory Method", "ShippingPolicyFactory indexa as estrategias por modalidade",
                    "shipping/ShippingPolicyFactory.java"),
            new Pattern("Strategy", "ShippingPolicy: uma classe por modalidade de frete",
                    "shipping"),
            new Pattern("Chain of Responsibility", "CheckoutRule + @Order: regras encadeadas pelo Spring",
                    "rules"),
            new Pattern("Decorator", "PriceDecorator: cada beneficio embrulha o calculo anterior",
                    "pricing"),
            new Pattern("Facade", "CheckoutService esconde a sequencia inteira do checkout",
                    "checkout/CheckoutService.java"),
            new Pattern("Observer", "ApplicationEventPublisher + @EventListener avisam sem acoplar",
                    "events"),
            new Pattern("Template Method", "Notifier fixa o roteiro do aviso e varia as etapas",
                    "notification"),
            new Pattern("State", "OrderStatus decide quais transicoes aceita",
                    "order/OrderStatus.java"),
            new Pattern("Adapter", "LegacyBankPaymentAdapter traduz o banco legado",
                    "payment"),
            new Pattern("Proxy", "CachedProductQuery controla o acesso ao catalogo real",
                    "catalog/CachedProductQuery.java")
    );

    private final CheckoutRuleChain rules;
    private final CustomerNoticeListener notices;
    private final CachedProductQuery cache;

    public PatternsController(CheckoutRuleChain rules,
                              CustomerNoticeListener notices,
                              CachedProductQuery cache) {
        this.rules = rules;
        this.notices = notices;
        this.cache = cache;
    }

    public record Pattern(String padrao, String onde, String pacote) {
    }

    @GetMapping
    @Operation(summary = "Lista os padroes aplicados e onde cada um mora")
    public List<Pattern> patterns() {
        return CATALOG;
    }

    @GetMapping("/runtime")
    @Operation(summary = "Mostra o que o Spring montou: corrente de regras, canais e cache do proxy")
    public Map<String, Object> runtime() {
        return Map.of(
                "correnteDeRegras", rules.registered(),
                "canaisDeAviso", notices.notifiers().stream()
                        .map(notifier -> notifier.getClass().getSimpleName()).toList(),
                "proxyDoCatalogo", Map.of("hits", cache.hits(), "misses", cache.misses())
        );
    }
}
