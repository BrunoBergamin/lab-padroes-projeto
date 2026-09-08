package br.com.bergamin.checkout.rules;

import br.com.bergamin.checkout.pricing.CouponPolicy;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/** Cupom e opcional; quando vem, precisa existir. */
@Component
@Order(30)
public class CouponAcceptedRule implements CheckoutRule {

    private final CouponPolicy coupons;

    public CouponAcceptedRule(CouponPolicy coupons) {
        this.coupons = coupons;
    }

    @Override
    public void apply(CheckoutContext context) {
        String coupon = context.coupon();
        if (coupon == null || coupon.isBlank()) {
            return;
        }
        if (!coupons.exists(coupon)) {
            reject("cupom invalido ou expirado: " + coupon);
        }
    }
}
