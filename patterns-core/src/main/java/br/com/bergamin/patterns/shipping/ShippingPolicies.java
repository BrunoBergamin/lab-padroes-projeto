package br.com.bergamin.patterns.shipping;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Factory Method.
 *
 * Quem faz o checkout escolhe uma modalidade, nao uma classe. A fabrica e o unico ponto do
 * codigo que precisa saber qual implementacao atende cada enum.
 */
public final class ShippingPolicies {

    private static final Map<ShippingMode, ShippingPolicy> POLICIES = List.of(
            new StorePickup(),
            new StandardShipping(),
            new ExpressShipping()
    ).stream().collect(Collectors.toUnmodifiableMap(ShippingPolicy::mode, Function.identity()));

    private ShippingPolicies() {
    }

    public static ShippingPolicy of(ShippingMode mode) {
        ShippingPolicy policy = POLICIES.get(mode);
        if (policy == null) {
            throw new IllegalArgumentException("modalidade de frete sem politica: " + mode);
        }
        return policy;
    }
}
