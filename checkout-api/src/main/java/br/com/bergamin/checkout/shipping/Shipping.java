package br.com.bergamin.checkout.shipping;

import java.math.BigDecimal;

public record Shipping(String label, BigDecimal cost, int days) {
}
