package br.com.bergamin.checkout;

import br.com.bergamin.checkout.catalog.CachedProductQuery;
import br.com.bergamin.checkout.catalog.Product;
import br.com.bergamin.checkout.catalog.ProductRepository;
import br.com.bergamin.checkout.order.OrderRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** A API de ponta a ponta: o que o cliente HTTP ve de cada padrao. */
@SpringBootTest
@AutoConfigureMockMvc
class CheckoutApiTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    @Autowired
    private ProductRepository products;

    @Autowired
    private OrderRepository orders;

    @Autowired
    private CachedProductQuery cache;

    @BeforeEach
    void seed() {
        orders.deleteAll();
        products.deleteAll();
        products.saveAll(List.of(
                new Product("TST-01", "Item caro", new BigDecimal("100.00"), 10),
                new Product("TST-02", "Item barato", new BigDecimal("25.00"), 2)
        ));
        cache.evictAll();
    }

    private static final String PEDIDO_OK = """
            {
              "cliente": "Bruno",
              "cep": "13010-000",
              "entrega": "STANDARD",
              "cupom": "BEMVINDA10",
              "fidelidade": false,
              "itens": { "TST-01": 2 }
            }
            """;

    private String fecharPedido() throws Exception {
        MvcResult result = mvc.perform(post("/api/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PEDIDO_OK))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode body = json.readTree(result.getResponse().getContentAsString());
        return body.get("pedido").asText();
    }

    @Test
    @DisplayName("POST /api/checkout devolve 201 com o extrato do preco")
    void checkoutFeliz() throws Exception {
        mvc.perform(post("/api/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PEDIDO_OK))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.total").value(200.61))
                .andExpect(jsonPath("$.status").value("Aguardando pagamento"))
                .andExpect(jsonPath("$.extrato[0].descricao").value("Itens"));
    }

    @Test
    @DisplayName("regra da corrente barrada vira 422 dizendo qual regra reprovou")
    void regraBarradaVira422() throws Exception {
        String abaixoDoMinimo = """
                {
                  "cliente": "Bruno",
                  "cep": "13010-000",
                  "entrega": "STANDARD",
                  "fidelidade": false,
                  "itens": { "TST-02": 1 }
                }
                """;

        mvc.perform(post("/api/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(abaixoDoMinimo))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.origem").value("MinimumAmountRule"))
                .andExpect(jsonPath("$.detalhe").value(org.hamcrest.Matchers.containsString("pedido minimo")));
    }

    @Test
    @DisplayName("corpo invalido vira 400 antes de chegar no servico")
    void corpoInvalidoVira400() throws Exception {
        String semCliente = """
                {
                  "cep": "13010-000",
                  "entrega": "STANDARD",
                  "itens": { "TST-01": 2 }
                }
                """;

        mvc.perform(post("/api/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(semCliente))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.origem").value("Validacao"));
    }

    @Test
    @DisplayName("State pela API: pagar, enviar e entao nao poder cancelar")
    void cicloDeVidaDoPedido() throws Exception {
        String id = fecharPedido();

        mvc.perform(post("/api/pedidos/" + id + "/pagamento"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Pago"));

        mvc.perform(post("/api/pedidos/" + id + "/envio"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Enviado"));

        mvc.perform(post("/api/pedidos/" + id + "/cancelamento"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.origem").value("OrderStatus"));
    }

    @Test
    @DisplayName("enviar antes de pagar vira 409")
    void enviarAntesDePagarVira409() throws Exception {
        String id = fecharPedido();

        mvc.perform(post("/api/pedidos/" + id + "/envio"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("pedido inexistente vira 404")
    void pedidoInexistenteVira404() throws Exception {
        mvc.perform(get("/api/pedidos/11111111-1111-1111-1111-111111111111"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/padroes serve de indice do projeto")
    void listaDePadroes() throws Exception {
        mvc.perform(get("/api/padroes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(12));

        mvc.perform(get("/api/padroes/runtime"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correnteDeRegras[0]").value("StockAvailableRule"))
                .andExpect(jsonPath("$.correnteDeRegras[1]").value("MinimumAmountRule"));
    }

    @Test
    @DisplayName("o estoque cai no catalogo depois do checkout")
    void estoqueCaiDepoisDoCheckout() throws Exception {
        fecharPedido();

        assertThat(products.findById("TST-01").orElseThrow().getStock()).isEqualTo(8);
    }
}
