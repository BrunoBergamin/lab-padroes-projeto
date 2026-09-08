package br.com.bergamin.checkout.pricing;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Montador da pilha de Decorators.
 *
 * A ordem importa e fica explicita aqui: cupom incide sobre itens mais frete, o frete gratis
 * entra depois e o cashback e o ultimo a embrulhar. Nenhum beneficio conhece os outros.
 */
@Component
public class PriceAssembler {

    private final CouponPolicy coupons;
    private final BigDecimal freeShippingFrom;
    private final BigDecimal cashbackRate;
    private final BigDecimal cashbackCap;

    public PriceAssembler(CouponPolicy coupons,
                          @Value("${checkout.frete-gratis-a-partir-de}") BigDecimal freeShippingFrom,
                          @Value("${checkout.cashback.percentual}") BigDecimal cashbackRate,
                          @Value("${checkout.cashback.teto}") BigDecimal cashbackCap) {
        this.coupons = coupons;
        this.freeShippingFrom = freeShippingFrom;
        this.cashbackRate = cashbackRate;
        this.cashbackCap = cashbackCap;
    }

    public PriceCalculation assemble(BigDecimal itemsSubtotal,
                                     BigDecimal shippingCost,
                                     String coupon,
                                     boolean loyaltyMember) {
        PriceCalculation price = new BasePrice(itemsSubtotal, shippingCost);

        BigDecimal percentage = coupons.percentageOf(coupon).orElse(BigDecimal.ZERO);
        if (percentage.signum() > 0) {
            price = new CouponDiscount(price, coupon, percentage);
        }

        price = new FreeShippingOver(price, freeShippingFrom, itemsSubtotal, shippingCost);

        if (loyaltyMember) {
            price = new LoyaltyCashback(price, cashbackRate, cashbackCap);
        }
        return price;
    }
}
