package org.octopusden.employee.config

import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.boot.actuate.autoconfigure.endpoint.web.CorsEndpointProperties
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementPortType
import org.springframework.boot.actuate.endpoint.web.EndpointLinksResolver
import org.springframework.boot.actuate.endpoint.web.EndpointMapping
import org.springframework.boot.actuate.endpoint.web.EndpointMediaTypes
import org.springframework.boot.actuate.endpoint.web.WebEndpointsSupplier
import org.springframework.boot.actuate.endpoint.web.annotation.ControllerEndpointsSupplier
import org.springframework.boot.actuate.endpoint.web.annotation.ServletEndpointsSupplier
import org.springframework.boot.actuate.endpoint.web.servlet.WebMvcEndpointHandlerMapping
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.util.StringUtils
import springfox.documentation.builders.ApiInfoBuilder
import springfox.documentation.builders.AuthorizationScopeBuilder
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.service.ApiKey
import springfox.documentation.service.SecurityReference
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spi.service.contexts.SecurityContext
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2

private const val SECURITY_REFERENCE_NAME = "Authorization-Key"
private const val AUTH_SCOPE = "global"
private const val AUTH_SCOPE_DESC = "full access"
private const val AUTH_HEADER = "Authorization"
private const val SERVICE_NAME = "Employee Service"
private const val SERVICE_DESCRIPTION = "Employee Service API"
private const val SERVICE_API_VERSTION = ""

@Configuration
@EnableSwagger2
class SwaggerConfig {
    @Bean
    fun api(): Docket = Docket(DocumentationType.SWAGGER_2)
        .select()
        .apis(
            RequestHandlerSelectors.any()
        )
        .paths(PathSelectors.any())
        .build()
        .pathMapping("/")
        .securitySchemes(listOf(ApiKey(SECURITY_REFERENCE_NAME, AUTH_HEADER, SecurityScheme.In.HEADER.toString())))
        .securityContexts(listOf(
            SecurityContext.builder()
                .securityReferences(listOf(
                    SecurityReference.builder()
                        .reference(SECURITY_REFERENCE_NAME)
                        .scopes(arrayOf(
                            AuthorizationScopeBuilder()
                                .scope(AUTH_SCOPE)
                                .description(AUTH_SCOPE_DESC).build()
                        ))
                        .build()
                ))
                .build()
        ))
        .apiInfo(
            ApiInfoBuilder()
                .title(SERVICE_NAME)
                .description(SERVICE_DESCRIPTION)
                .version(SERVICE_API_VERSTION)
                .build()
        )
        .select()
        .build()

    @Deprecated("This is workaround for swagger")
    @Bean
    fun webEndpointServletHandlerMapping(
        webEndpointsSupplier: WebEndpointsSupplier,
        servletEndpointsSupplier: ServletEndpointsSupplier,
        controllerEndpointsSupplier: ControllerEndpointsSupplier,
        endpointMediaTypes: EndpointMediaTypes?,
        corsProperties: CorsEndpointProperties,
        webEndpointProperties: WebEndpointProperties, environment: Environment
    ): WebMvcEndpointHandlerMapping? {
        val webEndpoints = webEndpointsSupplier.endpoints
        val basePath = webEndpointProperties.basePath

        val allEndpoints = webEndpoints + servletEndpointsSupplier.endpoints + controllerEndpointsSupplier.endpoints

        val endpointMapping = EndpointMapping(basePath)
        val shouldRegisterLinksMapping = shouldRegisterLinksMapping(webEndpointProperties, environment, basePath)
        return WebMvcEndpointHandlerMapping(
            endpointMapping, webEndpoints, endpointMediaTypes,
            corsProperties.toCorsConfiguration(), EndpointLinksResolver(allEndpoints, basePath),
            shouldRegisterLinksMapping
        )
    }

    private fun shouldRegisterLinksMapping(
        webEndpointProperties: WebEndpointProperties, environment: Environment, basePath: String
    ): Boolean = webEndpointProperties.discovery.isEnabled && (StringUtils.hasText(basePath)
            || ManagementPortType.get(environment) == ManagementPortType.DIFFERENT)
}
