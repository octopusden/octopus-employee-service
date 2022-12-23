package org.octopusden.employee.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties("jira1")
@ConstructorBinding
data class Jira1Properties(val host: String, val username: String, val password: String)
