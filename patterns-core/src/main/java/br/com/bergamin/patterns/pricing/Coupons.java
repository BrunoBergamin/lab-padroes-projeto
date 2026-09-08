package br.com.bergamin.patterns.pricing;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

/** Cupons aceitos e o percentual de cada um. Em producao isso viria do banco. */
public final class Coupons {

    private static final Map<String, BigDecimal> ACTIVE = Map.of(
            "BEMVINDA10", new BigDecimal("0.10"),
            "FRETEFREE", BigDecimal.ZERO,
            "BLACK25", new BigDecimal("0.25")
    );

    private Coupons() {
    }

    public static boolean exists(String code) {
        return code != null && ACTIVE.containsKey(code.toUpperCase());
    }

    public static Optional<BigDecimal> percentageOf(String code) {
        if (code == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(ACTIVE.get(code.toUpperCase()));
    }
}
