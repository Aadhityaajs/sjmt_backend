package com.sjmt.SJMT.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;

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
            .info(new Info()
                .title("SJMT Backend API")
                .version("1.0")
                .description("SJMT Backend API with JWT Authentication, Role-Based Access Control, and Email Verification")
                .contact(new Contact()
                    .name("SJMT Team")
                    .email("support@sjmt.com")))
            .components(new Components()
                .addSecuritySchemes("Bearer Authentication", 
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Enter JWT token obtained from login endpoint")));
    }
}