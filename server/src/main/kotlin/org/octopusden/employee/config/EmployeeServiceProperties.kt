package org.octopusden.employee.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("employee-service")
data class EmployeeServiceProperties(val workDayHours: Int, val userAvailability: UserAvailability) {

    data class UserAvailability (val jql: String)

}
