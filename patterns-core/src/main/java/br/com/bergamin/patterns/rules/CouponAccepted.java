package br.com.bergamin.patterns.rules;

import br.com.bergamin.patterns.pricing.Coupons;

/** Cupom e opcional; quando vem, precisa existir. */
public final class CouponAccepted extends CheckoutRule {

    @Override
    protected void apply(CheckoutContext context) {
        String coupon = context.coupon();
        if (coupon == null || coupon.isBlank()) {
            return;
        }
        if (!Coupons.exists(coupon)) {
            reject("cupom invalido ou expirado: " + coupon);
        }
    }
}
