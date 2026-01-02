package com.hags.qrcert.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("QR Certificate System API")
                .version("1.0.0")
                .description("API for generating QR codes and managing card certificates with anti-tamper protection")
                .contact(new Contact()
                    .name("HAGS Grading")
                    .url("https://www.hags-grading.co.uk"))
                .license(new License()
                    .name("Proprietary")
                    .url("https://www.hags-grading.co.uk")));
    }
}

