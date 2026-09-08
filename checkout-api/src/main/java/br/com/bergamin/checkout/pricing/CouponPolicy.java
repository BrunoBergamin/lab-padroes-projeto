package br.com.bergamin.checkout.pricing;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

/** Cupons ativos. Em producao viria do banco; aqui basta um bean com o mapa. */
@Component
public class CouponPolicy {

    private static final Map<String, BigDecimal> ACTIVE = Map.of(
            "BEMVINDA10", new BigDecimal("0.10"),
            "BLACK25", new BigDecimal("0.25")
    );

    public boolean exists(String code) {
        return code != null && ACTIVE.containsKey(code.toUpperCase());
    }

    public Optional<BigDecimal> percentageOf(String code) {
        return code == null ? Optional.empty() : Optional.ofNullable(ACTIVE.get(code.toUpperCase()));
    }
}
