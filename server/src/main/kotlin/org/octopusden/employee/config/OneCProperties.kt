package org.octopusden.employee.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("one-c")
data class OneCProperties(val host: String, val username: String, val password: String)
