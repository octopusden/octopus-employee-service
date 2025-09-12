package org.octopusden.employee.config

import org.octopusden.cloud.commons.security.client.AuthServerClient
import org.octopusden.cloud.commons.security.config.CloudCommonWebSecurityConfig
import org.octopusden.cloud.commons.security.config.SecurityProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(AuthServerClient::class)
@EnableConfigurationProperties(SecurityProperties::class)
class WebSecurityConfig(
    authServerClient: AuthServerClient,
    securityProperties: SecurityProperties,
) : CloudCommonWebSecurityConfig(
    authServerClient = authServerClient,
    securityProperties = securityProperties,
)
