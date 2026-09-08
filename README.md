# Padrões de Projeto na prática — Motor de Checkout

Projeto do desafio final do módulo de **Padrões de Projeto** (DIO / trilha Itaú Java com IA).
Em vez de um exemplo por padrão solto, os padrões estão todos dentro de **um problema só**:
fechar o pedido de uma loja online.

O mesmo motor foi escrito **duas vezes**, de propósito:

| Módulo | O que é | Para que serve |
|---|---|---|
| `patterns-core` | Java puro, sem framework | Ver o padrão "no osso": eu monto a corrente, o barramento de eventos e as fábricas na mão |
| `checkout-api` | Spring Boot 3 + JPA + H2 + Swagger | Ver o mesmo padrão quando **o framework já faz metade do trabalho** (injeção, eventos, transação) |

Comparar os dois é o ponto do projeto: o padrão não desaparece com o Spring, ele só muda de forma.

---

## Rodando

Precisa de **JDK 21+** (testado no 25). O Maven vem junto pelo wrapper.

```bash
# 1) roda os testes dos dois módulos (61 testes)
./mvnw test

# 2) demo do módulo Java puro: imprime um checkout inteiro no console
./mvnw -q -pl patterns-core compile
java -cp patterns-core/target/classes br.com.bergamin.patterns.Demo

# 3) sobe a API Spring
./mvnw -pl checkout-api spring-boot:run
```

Com a API no ar:

- **Swagger UI** → http://localhost:8080/docs
- **Console do H2** → http://localhost:8080/h2 (url `jdbc:h2:mem:checkout`, usuário `sa`, sem senha)
- **Mapa dos padrões** → http://localhost:8080/api/padroes

### Fechando um pedido pelo terminal

```bash
curl -X POST http://localhost:8080/api/checkout \
  -H "Content-Type: application/json" \
  -d '{
        "cliente": "Bruno Bergamin",
        "cep": "13010-000",
        "entrega": "STANDARD",
        "cupom": "BEMVINDA10",
        "fidelidade": true,
        "itens": { "BAT-001": 2, "SER-014": 1 }
      }'
```

Resposta (resumida) — repare no **extrato**, que é a pilha de Decorators aberta linha a linha:

```json
{
  "pedido": "9fee3248-8078-4187-a88f-bc02455528f5",
  "status": "Aguardando pagamento",
  "entrega": "Entrega economica",
  "prazoEmDias": 5,
  "extrato": [
    { "descricao": "Itens",                    "valor":  208.80 },
    { "descricao": "Frete",                    "valor":   24.40 },
    { "descricao": "Cupom BEMVINDA10",         "valor":  -23.32 },
    { "descricao": "Cashback fidelidade",      "valor":  -10.49 }
  ],
  "total": 199.39
}
```

Depois dá para andar com o pedido (isso é o **State**):

```bash
curl -X POST http://localhost:8080/api/pedidos/{id}/pagamento     # 200 → "Pago"
curl -X POST http://localhost:8080/api/pedidos/{id}/envio         # 200 → "Enviado"
curl -X POST http://localhost:8080/api/pedidos/{id}/cancelamento  # 409 → enviado não cancela
```

---

## Os 12 padrões e onde cada um mora

A explicação completa de cada um (o problema, o código que existiria sem ele e a diferença entre
os dois módulos) está em **[docs/PADROES.md](docs/PADROES.md)**. Resumo:

| Padrão | Problema que ele resolve aqui | Java puro | Spring |
|---|---|---|---|
| **Singleton** | Um catálogo só na aplicação inteira | `catalog/Catalog.java` | escopo padrão do bean |
| **Builder** | Pedido com muitos campos, sem construtor gigante | `order/Order.java` | `order/Order.java` |
| **Factory Method** | Escolher a política de frete pela modalidade | `shipping/ShippingPolicies.java` | `shipping/ShippingPolicyFactory.java` |
| **Strategy** | Cada frete calcula de um jeito | `shipping/` | `shipping/` |
| **Chain of Responsibility** | Várias validações independentes antes de fechar | `rules/CheckoutRule.java` | `rules/` + `@Order` |
| **Decorator** | Descontos que se acumulam em cima do preço | `pricing/PriceDecorator.java` | `pricing/PriceAssembler.java` |
| **Facade** | Uma porta de entrada para um processo de 6 etapas | `CheckoutFacade.java` | `checkout/CheckoutService.java` |
| **Observer** | Avisar e-mail/SMS/auditoria sem o checkout saber deles | `events/EventBus.java` | `@EventListener` |
| **Template Method** | Todo aviso segue o mesmo roteiro, muda o canal | `notification/Notifier.java` | `notification/Notifier.java` |
| **State** | Pedido só aceita as transições que fazem sentido | `order/state/` | `order/OrderStatus.java` |
| **Adapter** | Falar com a API velha do banco sem sujar o domínio | — | `payment/` |
| **Proxy** | Segurar consulta de produto em cache | — | `catalog/CachedProductQuery.java` |

> Adapter e Proxy só aparecem no módulo Spring — são os dois padrões que ficam mais naturais
> quando existe injeção de dependência para trocar a implementação sem ninguém perceber.

---

## Estrutura

```
lab-padroes-projeto/
├── patterns-core/                  # Java puro
│   └── src/main/java/br/com/bergamin/patterns/
│       ├── catalog/                # Singleton
│       ├── order/ + order/state/   # Builder e State
│       ├── pricing/                # Decorator
│       ├── rules/                  # Chain of Responsibility
│       ├── shipping/               # Strategy + Factory Method
│       ├── events/                 # Observer
│       ├── notification/           # Template Method
│       ├── CheckoutFacade.java     # Facade
│       └── Demo.java               # roda tudo no console
│
├── checkout-api/                   # Spring Boot
│   └── src/main/java/br/com/bergamin/checkout/
│       ├── catalog/                # Proxy (cache) + JPA
│       ├── checkout/               # Facade (service) + controller + DTOs
│       ├── config/                 # tratamento de erro, seed, OpenAPI
│       ├── events/                 # Observer com eventos do Spring
│       ├── notification/           # Template Method
│       ├── order/                  # Builder + State (enum persistido)
│       ├── patterns/               # /api/padroes: índice vivo do projeto
│       ├── payment/                # Adapter do banco legado
│       ├── pricing/                # Decorator
│       ├── rules/                  # Chain of Responsibility com @Order
│       └── shipping/               # Strategy + Factory
│
└── docs/PADROES.md                 # o porquê de cada padrão
```

## Regras de negócio que o motor aplica

Estão todas em um lugar só (`rules/`) e valem para os dois módulos:

1. **Estoque** — não fecha pedido de item que não tem.
2. **Pedido mínimo** — R$ 50,00 (configurável no `application.yml`).
3. **Cupom** — é opcional, mas se vier tem que existir (`BEMVINDA10` 10%, `BLACK25` 25%).
4. **Região** — entrega exige CEP válido; alguns CEPs ainda não são atendidos. Retirada na loja dispensa CEP.

E os benefícios de preço, aplicados **nesta ordem** (a ordem muda o total, por isso ela é explícita):
cupom → frete grátis acima de R$ 299 → cashback de 5% (teto de R$ 30) para cliente fidelidade.

## Testes

```
patterns-core   28 testes
checkout-api    33 testes  (11 da fachada, 8 da API via MockMvc, 14 unitários)
```

Cada teste tem um `@DisplayName` em português dizendo **qual padrão** ele está exercitando —
dá para ler a lista de testes como se fosse a documentação do motor.

---

## O que eu tirei desse desafio

- Padrão não é enfeite: cada um aqui entrou porque tinha um `if` crescendo ou uma classe sabendo
  demais sobre as outras. Em `docs/PADROES.md` eu escrevi, para cada padrão, **como seria o código sem ele**.
- Muito padrão clássico já vem embutido no Spring. Saber o padrão é o que faz entender *por que*
  `@Order`, `@EventListener` ou `@Primary` existem — em vez de decorar anotação.
- A ordem em que se empilha Decorator é regra de negócio, não detalhe técnico.

Feito por **Bruno Alves Bergamin** — desafio da trilha Itaú Java com IA (DIO).
