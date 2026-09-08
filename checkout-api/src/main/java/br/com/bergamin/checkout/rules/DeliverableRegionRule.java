package br.com.bergamin.checkout.rules;

import br.com.bergamin.checkout.shipping.ShippingMode;
import br.com.bergamin.checkout.shipping.ZipCodes;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Set;

/** Retirada dispensa CEP; entrega exige CEP valido e regiao atendida. */
@Component
@Order(40)
public class DeliverableRegionRule implements CheckoutRule {

    private static final Set<String> BLOCKED_PREFIXES = Set.of("69", "88", "89");

    @Override
    public void apply(CheckoutContext context) {
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
