package org.octopusden.employee.config

import java.time.LocalDate
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.format.annotation.DateTimeFormat

@ConfigurationProperties("one-c")
@ConstructorBinding
data class OneCProperties(
    val host: String,
    val username: String,
    val password: String,
    val health: Health? = null
) {
    data class Health(
        val user: String,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        val startDate: LocalDate,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        val endDate: LocalDate
    )
}
