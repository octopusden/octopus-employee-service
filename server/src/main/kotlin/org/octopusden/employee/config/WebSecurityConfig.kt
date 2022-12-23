package org.octopusden.employee.config

import org.octopusden.cloud.commons.security.client.AuthServerClient
import org.octopusden.cloud.commons.security.config.CloudCommonWebSecurityConfig
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(AuthServerClient::class)
class WebSecurityConfig(authServerClient: AuthServerClient) : CloudCommonWebSecurityConfig(authServerClient)
