package br.com.bergamin.checkout.shipping;

import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Factory.
 *
 * O Spring entrega a lista de estrategias registradas; a fabrica so indexa por modalidade.
 * Uma transportadora nova entra no sistema criando uma classe, sem editar esta aqui.
 */
@Component
public class ShippingPolicyFactory {

    private final Map<ShippingMode, ShippingPolicy> policies = new EnumMap<>(ShippingMode.class);

    public ShippingPolicyFactory(List<ShippingPolicy> available) {
        available.forEach(policy -> policies.put(policy.mode(), policy));
    }

    public ShippingPolicy of(ShippingMode mode) {
        ShippingPolicy policy = policies.get(mode);
        if (policy == null) {
            throw new IllegalArgumentException("modalidade de frete sem politica: " + mode);
        }
        return policy;
    }
}
