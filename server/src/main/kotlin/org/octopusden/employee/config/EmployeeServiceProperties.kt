package org.octopusden.employee.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("employee-service")
data class EmployeeServiceProperties(
    val workDayHours: Int,
    val userAvailability: UserAvailability,
) {
    data class UserAvailability(
        val jql: String,
    ) {
        init {
            // The availability endpoint turns this "absent today" query into a period query by replacing these functions
            require(START_OF_DAY in jql && END_OF_DAY in jql) {
                "employee-service.user-availability.jql must contain $START_OF_DAY and $END_OF_DAY"
            }
        }

        companion object {
            const val START_OF_DAY = "startOfDay()"
            const val END_OF_DAY = "endOfDay()"
        }
    }
}
