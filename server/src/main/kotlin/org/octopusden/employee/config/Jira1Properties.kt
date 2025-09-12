package org.octopusden.employee.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("jira1")
data class Jira1Properties(val host: String, val username: String, val password: String)
