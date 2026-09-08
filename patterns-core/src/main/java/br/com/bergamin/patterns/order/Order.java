package br.com.bergamin.patterns.order;

import br.com.bergamin.patterns.order.state.AwaitingPayment;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Builder + State.
 *
 * O pedido tem seis campos e nenhum pode faltar. Um construtor com seis argumentos na mesma
 * ordem e um convite a trocar o cupom pelo CEP sem o compilador reclamar; o Builder nomeia
 * cada um. O status fica delegado ao objeto de estado (ver OrderState).
 */
public final class Order {

    private final String id;
    private final String customer;
    private final List<OrderItem> items;
    private final String zipCode;
    private final String shippingLabel;
    private final BigDecimal total;

    private OrderState state;

    private Order(Builder builder) {
        this.id = builder.id;
        this.customer = builder.customer;
        this.items = List.copyOf(builder.items);
        this.zipCode = builder.zipCode;
        this.shippingLabel = builder.shippingLabel;
        this.total = builder.total;
        this.state = AwaitingPayment.INSTANCE;
    }

    public static Builder builder() {
        return new Builder();
    }

    public void pay() {
        this.state = state.pay();
    }

    public void ship() {
        this.state = state.ship();
    }

    public void cancel() {
        this.state = state.cancel();
    }

    public String status() {
        return state.name();
    }

    public String id() {
        return id;
    }

    public String customer() {
        return customer;
    }

    public List<OrderItem> items() {
        return items;
    }

    public String zipCode() {
        return zipCode;
    }

    public String shippingLabel() {
        return shippingLabel;
    }

    public BigDecimal total() {
        return total;
    }

    public static final class Builder {

        private String id = UUID.randomUUID().toString();
        private String customer;
        private final List<OrderItem> items = new ArrayList<>();
        private String zipCode;
        private String shippingLabel;
        private BigDecimal total = BigDecimal.ZERO;

        private Builder() {
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder customer(String customer) {
            this.customer = customer;
            return this;
        }

        public Builder item(OrderItem item) {
            this.items.add(item);
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

        public Builder shippingLabel(String shippingLabel) {
            this.shippingLabel = shippingLabel;
            return this;
        }

        public Builder total(BigDecimal total) {
            this.total = total;
            return this;
        }

        public Order build() {
            Objects.requireNonNull(customer, "customer e obrigatorio");
            Objects.requireNonNull(zipCode, "zipCode e obrigatorio");
            if (items.isEmpty()) {
                throw new IllegalStateException("pedido sem itens");
            }
            return new Order(this);
        }
    }
}
