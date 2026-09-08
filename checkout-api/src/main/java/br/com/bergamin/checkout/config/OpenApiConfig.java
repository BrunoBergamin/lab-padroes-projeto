package br.com.bergamin.checkout.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI checkoutOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Checkout API")
                .version("1.0.0")
                .description("Motor de checkout com padroes de projeto: Strategy, Chain of Responsibility, "
                        + "Decorator, Facade, Adapter, Proxy, Observer, Template Method, State e Factory."));
    }
}
