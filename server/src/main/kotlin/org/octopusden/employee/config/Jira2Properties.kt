package org.octopusden.employee.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("jira2")
data class Jira2Properties(val host: String, val username: String, val password: String)
