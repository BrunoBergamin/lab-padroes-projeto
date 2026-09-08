package br.com.bergamin.checkout.checkout;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/** A API expoe a fachada: um POST fecha o pedido inteiro. */
@RestController
@RequestMapping("/api")
@Tag(name = "Checkout")
public class CheckoutController {

    private final CheckoutService checkout;

    public CheckoutController(CheckoutService checkout) {
        this.checkout = checkout;
    }

    @PostMapping("/checkout")
    @Operation(summary = "Fecha o pedido: regras, frete, preco, estoque e avisos")
    public ResponseEntity<CheckoutResponse> checkout(@Valid @RequestBody CheckoutRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(checkout.checkout(request));
    }

    @GetMapping("/pedidos")
    @Operation(summary = "Lista os pedidos")
    public List<OrderView> orders() {
        return checkout.orders();
    }

    @GetMapping("/pedidos/{id}")
    @Operation(summary = "Consulta um pedido")
    public OrderView order(@PathVariable UUID id) {
        return checkout.order(id);
    }

    @PostMapping("/pedidos/{id}/pagamento")
    @Operation(summary = "Autoriza o pagamento no banco legado (Adapter) e move o estado")
    public OrderView pay(@PathVariable UUID id) {
        return checkout.pay(id);
    }

    @PostMapping("/pedidos/{id}/envio")
    @Operation(summary = "Marca o pedido como enviado")
    public OrderView ship(@PathVariable UUID id) {
        return checkout.ship(id);
    }

    @PostMapping("/pedidos/{id}/cancelamento")
    @Operation(summary = "Cancela o pedido")
    public OrderView cancel(@PathVariable UUID id) {
        return checkout.cancel(id);
    }
}
