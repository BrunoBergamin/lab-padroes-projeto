package br.com.bergamin.checkout.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
public class Product {

    @Id
    private String sku;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private int stock;

    protected Product() {
    }

    public Product(String sku, String name, BigDecimal unitPrice, int stock) {
        this.sku = sku;
        this.name = name;
        this.unitPrice = unitPrice;
        this.stock = stock;
    }

    public void reduceStock(int quantity) {
        if (quantity > stock) {
            throw new IllegalStateException("estoque insuficiente para " + sku);
        }
        this.stock -= quantity;
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

    public int getStock() {
        return stock;
    }
}
