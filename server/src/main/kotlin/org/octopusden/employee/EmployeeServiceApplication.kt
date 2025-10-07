package org.octopusden.employee

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.context.annotation.Configuration

@SpringBootApplication
@Configuration
@ConfigurationPropertiesScan
class EmployeeServiceApplication

fun main(args: Array<String>) {
    SpringApplication.run(EmployeeServiceApplication::class.java, *args)
}
