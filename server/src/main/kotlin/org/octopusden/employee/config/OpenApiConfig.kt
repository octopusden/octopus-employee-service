package org.octopusden.employee.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun springShopOpenAPI(): OpenAPI {
        val securitySchemeName = "bearerAuth"
        return OpenAPI().info(Info().title("Employee Service").description("Employee Service API"))
            .addSecurityItem(SecurityRequirement().addList(securitySchemeName)).components(
                Components().addSecuritySchemes(
                    securitySchemeName,
                    SecurityScheme().name(securitySchemeName).type(SecurityScheme.Type.HTTP).scheme("bearer")
                        .bearerFormat("JWT")
                )
            )
    }
}