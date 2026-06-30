package com.primesys.adminserviceserver.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Primesys Admin API")
                        .description("Primesys admin api config and schema available here").version("v1")
                        .contact(new Contact().name("Primesys India").url("http://localhost:8000/admin-service")
                                .email("primesysdemo@gmail.com"))
                        .termsOfService("yet to come").license(new License().name("API License")))

                // 🔐 Apply Bearer auth globally
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))

                // 🔐 Define Bearer JWT scheme
                .components(new io.swagger.v3.oas.models.Components().addSecuritySchemes(SECURITY_SCHEME_NAME,
                        new SecurityScheme().name(SECURITY_SCHEME_NAME).type(SecurityScheme.Type.HTTP).scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
