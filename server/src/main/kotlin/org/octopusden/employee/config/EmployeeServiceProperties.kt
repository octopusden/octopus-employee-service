package org.octopusden.employee.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties("employee-service")
@ConstructorBinding
data class EmployeeServiceProperties(val workDayHours: Int, val userAvailability: UserAvailability) {

    data class UserAvailability (val jql: String)

}
