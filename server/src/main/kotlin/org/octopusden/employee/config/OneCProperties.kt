package org.octopusden.employee.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties("one-c")
@ConstructorBinding
data class OneCProperties(val host: String, val username: String, val password: String)
