package br.com.bergamin.checkout.order;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Builder + State.
 *
 * O Builder monta o pedido com os campos nomeados; o State fica no enum OrderStatus, que
 * decide sozinho quais transicoes aceita.
 */
@Entity
@Table(name = "orders")
public class Order {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String customer;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "order_items", joinColumns = @JoinColumn(name = "order_id"))
    private List<OrderItem> items = new ArrayList<>();

    private String zipCode;

    private String shippingLabel;

    @Column(precision = 12, scale = 2)
    private BigDecimal shippingCost;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status;

    @Column(nullable = false)
    private Instant createdAt;

    protected Order() {
    }

    private Order(Builder builder) {
        this.id = UUID.randomUUID();
        this.customer = builder.customer;
        this.items = List.copyOf(builder.items);
        this.zipCode = builder.zipCode;
        this.shippingLabel = builder.shippingLabel;
        this.shippingCost = builder.shippingCost;
        this.total = builder.total;
        this.status = OrderStatus.AWAITING_PAYMENT;
        this.createdAt = Instant.now();
    }

    public static Builder builder() {
        return new Builder();
    }

    public void pay() {
        this.status = status.pay();
    }

    public void ship() {
        this.status = status.ship();
    }

    public void cancel() {
        this.status = status.cancel();
    }

    public UUID getId() {
        return id;
    }

    public String getCustomer() {
        return customer;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public String getZipCode() {
        return zipCode;
    }

    public String getShippingLabel() {
        return shippingLabel;
    }

    public BigDecimal getShippingCost() {
        return shippingCost;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public static final class Builder {

        private String customer;
        private final List<OrderItem> items = new ArrayList<>();
        private String zipCode;
        private String shippingLabel;
        private BigDecimal shippingCost = BigDecimal.ZERO;
        private BigDecimal total = BigDecimal.ZERO;

        private Builder() {
        }

        public Builder customer(String customer) {
            this.customer = customer;
            return this;
        }

        public Builder items(List<OrderItem> items) {
            this.items.addAll(items);
            return this;
        }

        public Builder zipCode(String zipCode) {
            this.zipCode = zipCode;
            return this;
        }

        public Builder shipping(String label, BigDecimal cost) {
            this.shippingLabel = label;
            this.shippingCost = cost;
            return this;
        }

        public Builder total(BigDecimal total) {
            this.total = total;
            return this;
        }

        public Order build() {
            Objects.requireNonNull(customer, "customer e obrigatorio");
            if (items.isEmpty()) {
                throw new IllegalStateException("pedido sem itens");
            }
            return new Order(this);
        }
    }
}
