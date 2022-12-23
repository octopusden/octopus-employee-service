package org.octopusden.employee.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties("jira2")
@ConstructorBinding
data class Jira2Properties(val host: String, val username: String, val password: String)
