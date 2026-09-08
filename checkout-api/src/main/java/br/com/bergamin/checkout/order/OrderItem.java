package br.com.bergamin.checkout.order;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;

@Embeddable
public class OrderItem {

    @Column(nullable = false)
    private String sku;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private int quantity;

    protected OrderItem() {
    }

    public OrderItem(String sku, String name, BigDecimal unitPrice, int quantity) {
        this.sku = sku;
        this.name = name;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    public BigDecimal subtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public String getSku() {
        return sku;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }
}
