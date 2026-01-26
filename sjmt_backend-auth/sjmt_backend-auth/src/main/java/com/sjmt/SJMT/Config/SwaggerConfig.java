package com.sjmt.SJMT.Config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

/**
 * Swagger/OpenAPI Configuration
 * @author SJMT Team
 * @version 1.0
 */
@Configuration
public class SwaggerConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .servers(List.of(
                new Server().url("http://localhost:3010").description("Local Development Server"),
                new Server().url("http://72.62.75.169:3010").description("VPS Server")
            ))
            .info(new Info()
                .title("SJMT Backend API")
                .version("1.0.0")
                .description("SJMT Backend API with JWT Authentication, Role-Based Access Control, and Email Verification")
                .contact(new Contact()
                    .name("SJMT Team")
                    .email("support@sjmt.com")
                    .url("https://github.com/Aadhityaajs/sjmt_backend"))
                .license(new License()
                    .name("Apache 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
            .components(new Components()
                .addSecuritySchemes("Bearer Authentication", 
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .in(SecurityScheme.In.HEADER)
                        .name("Authorization")
                        .description("Enter JWT token obtained from login endpoint. Format: Bearer {token}")));
    }
}