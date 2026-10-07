package org.octopusden.employee.config

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class EmployeeServicePropertiesTest {
    @Test
    fun shouldRejectJqlWithoutDayFunctions() {
        val exception = assertThrows<IllegalArgumentException> {
            EmployeeServiceProperties.UserAvailability("Employee in ({usernames}) AND \"Leave from date\" <= now()")
        }
        Assertions.assertEquals(
            "employee-service.user-availability.jql must contain startOfDay() and endOfDay()",
            exception.message,
        )
    }
}
