package br.com.bergamin.patterns;

import br.com.bergamin.patterns.shipping.ShippingMode;

import java.util.LinkedHashMap;
import java.util.Map;

/** O que chega do carrinho: sku para quantidade, mais os dados da entrega. */
public record CheckoutRequest(String customer,
                              String zipCode,
                              ShippingMode mode,
                              String coupon,
                              boolean loyaltyMember,
                              Map<String, Integer> items) {

    public CheckoutRequest {
        items = new LinkedHashMap<>(items);
    }
}
