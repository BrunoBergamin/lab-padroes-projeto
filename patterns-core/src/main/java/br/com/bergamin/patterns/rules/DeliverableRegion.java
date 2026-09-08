package br.com.bergamin.patterns.rules;

import br.com.bergamin.patterns.shipping.ShippingMode;
import br.com.bergamin.patterns.shipping.ZipCodes;

import java.util.Set;

/** Retirada dispensa CEP; entrega exige CEP valido e regiao atendida. */
public final class DeliverableRegion extends CheckoutRule {

    private static final Set<String> BLOCKED_PREFIXES = Set.of("69", "88", "89");

    @Override
    protected void apply(CheckoutContext context) {
        if (context.mode() == ShippingMode.PICKUP) {
            return;
        }
        String digits = ZipCodes.digitsOf(context.zipCode());
        if (!ZipCodes.isValid(digits)) {
            reject("CEP invalido: " + context.zipCode());
        }
        if (BLOCKED_PREFIXES.contains(digits.substring(0, 2))) {
            reject("ainda nao entregamos no CEP " + context.zipCode());
        }
    }
}
