# Por que cada padrão está aqui

Este arquivo é a parte que importa do projeto. Padrão de projeto decorado não serve para nada;
o que vale é reconhecer **o problema que ele resolve**. Então cada seção abaixo segue sempre a
mesma receita:

> **O problema** → **Como o padrão resolve** → **Onde está no código** → **Como seria sem ele** → **Java puro x Spring**

Referência do vocabulário: *Design Patterns* (GoF, 1994) e o uso que o Spring faz deles.

---

## 1. Singleton — *criacional*

**O problema.** O catálogo de produtos precisa ser o mesmo para a aplicação inteira. Se cada
parte do código criar o seu, um dá baixa no estoque e o outro não fica sabendo.

**Como resolve.** A classe controla a própria criação: construtor privado e um único ponto de
acesso.

**No código.** `patterns-core/catalog/Catalog.java`, na variação *initialization-on-demand holder*:

```java
private static final class Holder {
    private static final Catalog INSTANCE = new Catalog();
}
public static Catalog getInstance() {
    return Holder.INSTANCE;
}
```

A classe interna só é carregada na primeira chamada de `getInstance()`, e a JVM garante que
isso acontece **uma vez só**, sem `synchronized` no caminho de leitura.

**Sem ele.** `new Catalog()` espalhado; dois estoques diferentes na mesma execução.

**Java puro x Spring.** No Spring eu não escrevi nenhum Singleton: todo bean já é singleton do
contêiner por padrão. A diferença prática é grande — o singleton do Spring é uma instância **por
contêiner**, criada e destruída por ele, e por isso é fácil de trocar no teste. O `getInstance()`
estático é global de verdade, e é justamente por isso que ele atrapalha em teste.

---

## 2. Builder — *criacional*

**O problema.** O pedido tem cliente, itens, CEP, descrição do frete, custo do frete e total. Um
construtor com seis parâmetros, vários deles `String` e `BigDecimal`, é um convite a trocar dois de lugar sem o compilador
reclamar.

**Como resolve.** Separa a construção do objeto do objeto pronto, com métodos nomeados.

**No código.** `order/Order.java` (existe nos dois módulos, com a mesma ideia):

```java
Order.builder()
     .customer("Bruno")
     .items(itens)
     .zipCode("13010-000")
     .shipping("Entrega economica", new BigDecimal("22.90"))
     .total(total)
     .build();
```

O `build()` é o lugar certo para as invariantes: cliente obrigatório, pedido sem item não nasce.

**Sem ele.** `new Order(cliente, itens, cep, label, custo, total)` — e um dia alguém troca
`custo` por `total`.

---

## 3. Strategy — *comportamental*

**O problema.** Frete tem três formas de calcular (retirada, econômica, expressa) e amanhã tem uma
quarta.

**Como resolve.** Cada forma vira uma classe com a mesma interface; quem usa recebe a interface.

**No código.** `shipping/ShippingPolicy` com `StorePickup`, `StandardShipping` e `ExpressShipping`.

**Sem ele.** Aquele `switch (modalidade)` dentro do checkout, que cresce a cada transportadora nova
e obriga a mexer em código já testado.

**Java puro x Spring.** No Spring cada estratégia é um `@Component`. O detalhe bonito: o Spring
injeta **uma lista com todas** que ele encontrou:

```java
public ShippingPolicyFactory(List<ShippingPolicy> available) { ... }
```

Criar uma transportadora nova = criar uma classe. Nenhum arquivo existente é editado — isso é o
princípio aberto/fechado acontecendo de verdade.

---

## 4. Factory Method — *criacional*

**O problema.** Alguém tem que decidir *qual* Strategy usar. Se essa decisão ficar no checkout, o
checkout volta a conhecer todas as implementações.

**Como resolve.** Concentra a escolha em um único lugar: a fábrica.

**No código.** `shipping/ShippingPolicies.java` (puro, mapa estático) e
`shipping/ShippingPolicyFactory.java` (Spring, indexando a lista injetada por `ShippingMode`).

**Sem ele.** O `switch` volta — só que agora espalhado por vários serviços.

---

## 5. Chain of Responsibility — *comportamental*

**O problema.** Antes de fechar o pedido eu preciso conferir estoque, valor mínimo, cupom e região.
São checagens independentes, que mudam de tempos em tempos e que devem parar no primeiro erro.

**Como resolve.** Cada checagem é um elo; o pedido passa pela corrente até alguém barrar.

**No código (puro).** `rules/CheckoutRule.java` — cada regra guarda a referência do próximo elo:

```java
public final void check(CheckoutContext context) {
    apply(context);
    if (next != null) next.check(context);
}
```

**No código (Spring).** `rules/` — aqui a corrente é montada pelo framework. Cada regra é um
`@Component` com `@Order`, e o `CheckoutRuleChain` recebe a lista **já ordenada**:

```java
@Component @Order(10) class StockAvailableRule implements CheckoutRule { ... }
@Component @Order(20) class MinimumAmountRule   implements CheckoutRule { ... }
```

Repare no que mudou: no Java puro a regra conhece a próxima; no Spring **nenhuma regra conhece
nenhuma outra**. A ordem virou configuração. Isso é visível em `GET /api/padroes/runtime`, que
lista a corrente montada.

**Sem ele.** Um método `validar()` de 80 linhas com quatro blocos `if` que ninguém tem coragem de
mexer.

---

## 6. Decorator — *estrutural*

**O problema.** O total do pedido sofre benefícios que se acumulam: cupom, frete grátis acima de um
valor, cashback do programa de fidelidade. E **a ordem importa**: 10% sobre o valor com frete não é
a mesma coisa que 10% depois do frete grátis.

**Como resolve.** Cada benefício embrulha o cálculo anterior, respeitando a mesma interface.

**No código.** `pricing/PriceDecorator.java`:

```java
public BigDecimal total() {
    return inner.total().subtract(discount()).max(BigDecimal.ZERO);
}
```

E a montagem, que é onde a regra de negócio fica explícita (`pricing/PriceAssembler.java`):

```java
PriceCalculation price = new BasePrice(subtotal, frete);
if (temCupom)   price = new CouponDiscount(price, cupom, percentual);
                price = new FreeShippingOver(price, limite, subtotal, frete);
if (fidelidade) price = new LoyaltyCashback(price, taxa, teto);
```

Bônus: como cada camada sabe se descrever, dá para "abrir" a pilha e mostrar o extrato para o
cliente (`PriceBreakdown`) — que é exatamente o que a API devolve.

**Sem ele.** Um `calcularTotal()` com quatro `if` encadeados, onde mudar a ordem de dois descontos
significa reescrever o método.

---

## 7. Facade — *estrutural*

**O problema.** Fechar um pedido são seis etapas em ordem: montar itens, validar, cotar frete,
calcular preço, salvar, baixar estoque e publicar o evento. O controller não deveria conhecer nada
disso.

**Como resolve.** Uma porta de entrada única para um subsistema.

**No código.** `CheckoutFacade.checkout()` (puro) e `checkout/CheckoutService.checkout()` (Spring).
O controller inteiro é isto:

```java
@PostMapping("/checkout")
public ResponseEntity<CheckoutResponse> checkout(@Valid @RequestBody CheckoutRequest request) {
    return ResponseEntity.status(CREATED).body(checkout.checkout(request));
}
```

**Cuidado que eu tomei.** Fachada não é "classe que faz tudo". Ela **orquestra** e delega: quem
valida são as regras, quem calcula é o Decorator, quem cota é a Strategy. Se a lógica começar a
morar dentro da fachada, virou o famoso *God Service*.

---

## 8. Observer — *comportamental*

**O problema.** Depois do pedido fechado é preciso mandar e-mail, às vezes SMS, e registrar
auditoria. Amanhã entra integração com o ERP. Nada disso é responsabilidade do checkout.

**Como resolve.** O checkout publica um fato; quem se interessa se inscreve.

**No código (puro).** `events/EventBus.java`, escrito na mão — inclusive com a decisão de que um
ouvinte que estoura **não derruba o pedido**:

```java
try { listener.onCheckout(event); }
catch (RuntimeException e) { failures.add(...); }
```

**No código (Spring).** O barramento é o próprio contêiner:

```java
events.publishEvent(new CheckoutCompletedEvent(...));   // no service

@EventListener
public void on(CheckoutCompletedEvent event) { ... }     // em AuditLogListener
```

**Sem ele.** O serviço de checkout com `emailService`, `smsService`, `auditService` e `erpService`
injetados — e um teste de checkout que precisa de mock para quatro coisas que não têm a ver com
fechar pedido.

---

## 9. Template Method — *comportamental*

**O problema.** Todo aviso segue o mesmo roteiro: decidir se vale enviar → montar assunto → montar
corpo → entregar. O que muda é o canal.

**Como resolve.** A classe base fixa o roteiro (`final`) e deixa buracos para as subclasses.

**No código.** `notification/Notifier.java`:

```java
public final void send(CheckoutCompletedEvent event) {
    if (!shouldSend(event)) return;
    String message = channel() + " | " + subject(event) + " | " + body(event);
    deliver(message);
}
```

`EmailNotifier` sempre envia; `SmsNotifier` sobrescreve `shouldSend` e só manda acima de R$ 200,
porque SMS custa por mensagem. A **ordem** das etapas ninguém consegue mudar — é isso que o
`final` protege.

**Strategy x Template Method.** Os dois trocam comportamento. A diferença: Strategy troca o
**algoritmo inteiro** (composição, um objeto por dentro); Template Method troca **partes** de um
algoritmo cuja estrutura é fixa (herança).

---

## 10. State — *comportamental*

**O problema.** Um pedido aguardando pagamento pode ser pago ou cancelado; pago pode ser enviado ou
cancelado; enviado não volta atrás. Se isso virar `if` espalhado, um dia um pedido enviado é
cancelado.

**Como resolve.** Cada estado sabe as próprias transições.

**No código (puro).** `order/state/` — uma classe por estado (`AwaitingPayment`, `Paid`, `Shipped`,
`Cancelled`).

**No código (Spring).** `order/OrderStatus.java` — um `enum` com corpo por constante:

```java
AWAITING_PAYMENT("Aguardando pagamento") {
    @Override public OrderStatus pay()    { return PAID; }
    @Override public OrderStatus cancel() { return CANCELLED; }
},
```

**Por que mudei de classes para enum no Spring?** Porque o pedido é persistido. Um enum salva
limpo (`@Enumerated(EnumType.STRING)`) e continua sendo State de verdade: quem decide a transição
é o próprio estado, não um `if` no service. As transições proibidas caem no método padrão, que
lança `InvalidTransitionException` — e o `ApiExceptionHandler` transforma em **409 Conflict**.

---

## 11. Adapter — *estrutural*

**O problema.** A API do banco fala em centavos (`long`), exige código de moeda e devolve
`"OK;123"` ou `"ERR;motivo"`. Se esse dialeto entrar no domínio, todo o sistema aprende o vício de
um fornecedor.

**Como resolve.** Uma classe traduz a interface que eu tenho para a interface que eu quero.

**No código.** `payment/`:

- `LegacyBankApi` — o sistema legado (que eu finjo não poder alterar);
- `PaymentGateway` — a interface que **a minha aplicação** queria ter;
- `LegacyBankPaymentAdapter` — a tradução: `BigDecimal` → centavos, string → `PaymentResult`.

O serviço só conhece `PaymentGateway`. Trocar de banco = escrever outro adaptador; nenhuma linha do
checkout muda.

**Sem ele.** `if (resposta.startsWith("OK;"))` dentro do serviço de pedidos.

---

## 12. Proxy — *estrutural*

**O problema.** O mesmo SKU é consultado várias vezes no fechamento de um pedido — e a cada
consulta bate no banco.

**Como resolve.** Um objeto com **a mesma interface** do real fica na frente dele e controla o
acesso (cache, log, permissão, carregamento tardio).

**No código.** `catalog/`:

```java
public interface ProductQuery { ... }              // o contrato
@Component("jpaProductQuery") class JpaProductQuery // o objeto real
@Component @Primary class CachedProductQuery        // o proxy
```

O `@Primary` é a peça-chave: quem pede um `ProductQuery` recebe o **proxy**, sem saber. E o
`@Qualifier("jpaProductQuery")` no construtor do proxy é o que impede ele de injetar a si mesmo.

Cache pede invalidação, então quem muda estoque chama `evict(sku)` — sem isso, o proxy passa a
mentir. Isso está testado em `CatalogProxyTest`.

**Onde você já usou isso sem saber.** `@Transactional` e `@Cacheable` do Spring são proxies:
o objeto injetado não é a sua classe, é um proxy dela que abre transação antes e faz commit
depois. Entender Proxy é entender por que método privado anotado com `@Transactional` não funciona.

---

## Um resumo honesto

| Padrão | Sinal de que estava faltando |
|---|---|
| Singleton | duas instâncias com estados diferentes do mesmo recurso |
| Builder | construtor com muitos parâmetros do mesmo tipo |
| Strategy | `switch` que cresce a cada regra nova |
| Factory | a escolha da implementação aparecendo em vários lugares |
| Chain of Responsibility | método de validação virando uma pilha de `if` |
| Decorator | `calcularTotal()` que muda toda vez que entra uma promoção |
| Facade | controller conhecendo a ordem de seis passos |
| Observer | serviço principal injetando coisas que não são dele |
| Template Method | duas classes com o mesmo roteiro e três linhas diferentes |
| State | `if (status == X && status != Y)` espalhado |
| Adapter | vocabulário de um fornecedor vazando pro domínio |
| Proxy | mesma consulta repetida, ou log/segurança copiado em todo método |

**A armadilha oposta também é real:** padrão a mais é complexidade a mais. Se existe uma única
forma de calcular frete e não vai existir outra, `Strategy` só adiciona classe. Cada padrão aqui
entrou porque o problema tinha **variação de verdade** — três fretes, quatro regras, três
benefícios de preço, dois canais de aviso.
