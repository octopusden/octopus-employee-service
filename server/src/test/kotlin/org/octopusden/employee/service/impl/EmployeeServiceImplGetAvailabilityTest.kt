package org.octopusden.employee.service.impl

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito
import org.octopusden.employee.client.common.exception.BadRequestException
import org.octopusden.employee.config.EmployeeServiceProperties
import org.octopusden.employee.service.AdService
import org.octopusden.employee.service.OneCService
import org.octopusden.employee.service.jira.client.jira1.Jira1Client
import org.octopusden.employee.service.jira.client.jira2.Jira2Client
import org.springframework.beans.factory.ObjectProvider
import java.time.LocalDate
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EmployeeServiceImplGetAvailabilityTest {
    private val jira1Client = Mockito.mock(Jira1Client::class.java)
    private val jira2Client = Mockito.mock(Jira2Client::class.java)
    private val employeeService = EmployeeServiceImpl(
        Mockito.mock(OneCService::class.java),
        jira1Client,
        jira2Client,
        EmployeeServiceProperties(
            8,
            EmployeeServiceProperties.UserAvailability("\"Leave from date\" <= startOfDay() AND \"Leave to date\" >= endOfDay()"),
        ),
        Mockito.mock(ObjectProvider::class.java) as ObjectProvider<AdService>,
    )

    @ParameterizedTest
    @MethodSource("invalidRequests")
    fun invalidRequestIsRejectedBeforeAnyJiraCall(
        employees: Set<String>,
        fromDate: String,
        toDate: String,
    ) {
        Assertions.assertThrows(BadRequestException::class.java) {
            employeeService.getAvailability(employees, LocalDate.parse(fromDate), LocalDate.parse(toDate))
        }
        Mockito.verifyNoInteractions(jira1Client, jira2Client)
    }

    @Suppress("UnusedPrivateMember") // used by @MethodSource
    private fun invalidRequests(): Stream<Arguments> =
        Stream.of(
            Arguments.of(emptySet<String>(), "2021-12-01", "2021-12-31"),
            Arguments.of(setOf(""), "2021-12-01", "2021-12-31"),
            Arguments.of(setOf("employee", " "), "2021-12-01", "2021-12-31"),
            Arguments.of((1..16).map { "employee$it" }.toSet(), "2021-12-01", "2021-12-31"),
            Arguments.of(setOf("employee"), "2021-12-31", "2021-12-01"),
            Arguments.of(setOf("employee"), "2021-01-01", "2021-04-01"),
        )
}
