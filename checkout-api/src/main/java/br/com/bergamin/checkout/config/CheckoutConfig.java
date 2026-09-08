package br.com.bergamin.checkout.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/** Relogio como bean: os testes trocam por um Clock fixo sem gambiarra. */
@Configuration
public class CheckoutConfig {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
