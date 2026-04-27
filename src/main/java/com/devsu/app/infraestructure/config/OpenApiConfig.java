package com.devsu.app.infraestructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Bank API")
                        .description("API de gestión bancaria - Devsu")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Devsu")
                                .email("gederson.guzman@devsu.com")
                        )
                );
    }
}
