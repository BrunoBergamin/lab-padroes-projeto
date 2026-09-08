package br.com.bergamin.checkout.rules;

import org.springframework.stereotype.Component;

import java.util.List;

/** Percorre a corrente na ordem definida pelas anotacoes @Order de cada regra. */
@Component
public class CheckoutRuleChain {

    private final List<CheckoutRule> rules;

    public CheckoutRuleChain(List<CheckoutRule> rules) {
        this.rules = List.copyOf(rules);
    }

    public void check(CheckoutContext context) {
        rules.forEach(rule -> rule.apply(context));
    }

    public List<String> registered() {
        return rules.stream().map(rule -> rule.getClass().getSimpleName()).toList();
    }
}
