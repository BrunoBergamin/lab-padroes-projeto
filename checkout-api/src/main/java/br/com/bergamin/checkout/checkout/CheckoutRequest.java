package br.com.bergamin.checkout.checkout;

import br.com.bergamin.checkout.shipping.ShippingMode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

/** O que chega do carrinho: sku para quantidade, mais os dados da entrega. */
@Schema(name = "CheckoutRequest", description = "Carrinho pronto para fechar")
public record CheckoutRequest(

        @NotBlank
        @Schema(example = "Bruno Bergamin")
        String cliente,

        @Schema(example = "13010-000", description = "Obrigatorio quando a entrega nao e retirada")
        String cep,

        @NotNull
        @Schema(example = "STANDARD")
        ShippingMode entrega,

        @Schema(example = "BEMVINDA10", description = "Opcional")
        String cupom,

        @Schema(example = "true", description = "Cliente do programa de fidelidade")
        boolean fidelidade,

        @NotEmpty
        @Schema(example = "{\"BAT-001\": 2, \"SER-014\": 1}", description = "sku para quantidade")
        Map<String, Integer> itens) {
}
