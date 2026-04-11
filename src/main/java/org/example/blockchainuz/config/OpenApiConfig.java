package org.example.blockchainuz.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI blockchainUzOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("BlockchainUZ API")
                        .description("Blockchain explorer API for BlockchainUZ")
                        .version("1.0.0"));
    }
}
