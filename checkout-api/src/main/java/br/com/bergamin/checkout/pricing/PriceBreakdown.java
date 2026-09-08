package br.com.bergamin.checkout.pricing;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Percorre a pilha de decorators para montar o extrato que o cliente ve. */
public final class PriceBreakdown {

    public record Line(String descricao, BigDecimal valor) {
    }

    private PriceBreakdown() {
    }

    public static List<Line> of(PriceCalculation calculation) {
        List<Line> lines = new ArrayList<>();
        collect(calculation, lines);
        return lines;
    }

    private static void collect(PriceCalculation calculation, List<Line> lines) {
        if (calculation instanceof PriceDecorator decorator) {
            collect(decorator.inner(), lines);
            BigDecimal discount = decorator.discount();
            if (discount.signum() != 0) {
                lines.add(new Line(decorator.label(), discount.negate()));
            }
            return;
        }
        if (calculation instanceof BasePrice base) {
            lines.add(new Line("Itens", base.itemsSubtotal()));
            if (base.shippingCost().signum() != 0) {
                lines.add(new Line("Frete", base.shippingCost()));
            }
            return;
        }
        lines.add(new Line("Total", calculation.total()));
    }
}
