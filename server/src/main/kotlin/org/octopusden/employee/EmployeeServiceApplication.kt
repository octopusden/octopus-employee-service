package org.octopusden.employee

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.cloud.netflix.eureka.EnableEurekaClient
import org.springframework.context.annotation.Configuration

@EnableEurekaClient
@SpringBootApplication
@Configuration
@ConfigurationPropertiesScan
class EmployeeServiceApplication

fun main(args: Array<String>) {
    SpringApplication.run(EmployeeServiceApplication::class.java, *args)
}
