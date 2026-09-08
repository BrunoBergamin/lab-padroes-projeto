package br.com.bergamin.checkout.order;

/**
 * State, na versao que cabe dentro de uma coluna do banco.
 *
 * O enum guarda as transicoes em corpos por constante. O pedido persistido continua sendo
 * uma string, mas quem decide o que pode virar o que sao os proprios estados, nao um if
 * espalhado pelos services.
 */
public enum OrderStatus {

    AWAITING_PAYMENT("Aguardando pagamento") {
        @Override
        public OrderStatus pay() {
            return PAID;
        }

        @Override
        public OrderStatus cancel() {
            return CANCELLED;
        }
    },

    PAID("Pago") {
        @Override
        public OrderStatus ship() {
            return SHIPPED;
        }

        @Override
        public OrderStatus cancel() {
            return CANCELLED;
        }
    },

    SHIPPED("Enviado"),

    CANCELLED("Cancelado");

    private final String label;

    OrderStatus(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public OrderStatus pay() {
        throw new InvalidTransitionException(name(), "ser pago");
    }

    public OrderStatus ship() {
        throw new InvalidTransitionException(name(), "ser enviado");
    }

    public OrderStatus cancel() {
        throw new InvalidTransitionException(name(), "ser cancelado");
    }
}
