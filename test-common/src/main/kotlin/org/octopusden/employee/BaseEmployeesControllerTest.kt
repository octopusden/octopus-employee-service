package org.octopusden.employee

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.octopusden.employee.client.common.dto.Employee
import org.octopusden.employee.client.common.dto.EmployeeAvailabilityDTO
import org.octopusden.employee.client.common.dto.EmployeesAvailabilityDTO
import org.octopusden.employee.client.common.dto.UnavailableDayDTO
import org.octopusden.employee.client.common.dto.WorkingDaysDTO
import java.time.LocalDate
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class BaseEmployeesControllerTest : BaseTest() {
    @ParameterizedTest
    @MethodSource("availableEarlier")
    fun getEmployeeAvailableEarlierTest(
        employees: Set<String>,
        expectedEmployee: Employee,
    ) {
        val employeeAvailableEarlier = getEmployeeAvailableEarlier(employees)
        Assertions.assertEquals(expectedEmployee, employeeAvailableEarlier)
    }

    @ParameterizedTest
    @MethodSource("workingDays")
    fun getWorkingDaysTest(
        dateFrom: String,
        dateTo: String,
        expectedWorkingDays: Int,
    ) {
        val workingDays = getWorkingDays(dateFrom, dateTo)
        Assertions.assertEquals(expectedWorkingDays, workingDays.workingDays)
    }

    @ParameterizedTest
    @MethodSource("availability")
    fun getAvailabilityTest(
        employees: Set<String>,
        fromDate: String,
        toDate: String,
        expected: EmployeesAvailabilityDTO,
    ) {
        Assertions.assertEquals(expected, getAvailability(employees, fromDate.toLocalDate(), toDate.toLocalDate()))
    }

    protected abstract fun getEmployeeAvailableEarlier(employees: Set<String>): Employee

    protected abstract fun getAvailability(
        employees: Set<String>,
        fromDate: LocalDate,
        toDate: LocalDate,
    ): EmployeesAvailabilityDTO

    protected abstract fun getWorkingDays(
        fromDate: String,
        toDate: String,
    ): WorkingDaysDTO

    // <editor-fold defaultstate="collapsed" desc="test data">
    private fun availableEarlier(): Stream<Arguments> =
        Stream.of(
            Arguments.of(
                setOf("absent1", "absent2"),
                Employee("absent2", true),
            ),
            Arguments.of(
                setOf("absent1", "absent2", "employee"),
                Employee("employee", true),
            ),
        )

    @Suppress("UnusedPrivateMember") // used by @MethodSource
    private fun availability(): Stream<Arguments> {
        fun leave(
            from: String,
            to: String,
        ) = from
            .toLocalDate()
            .datesUntil(to.toLocalDate().plusDays(1))
            .map { date -> UnavailableDayDTO(date, UnavailableDayDTO.REASON_LEAVE) }
            .toList()
        return Stream.of(
            Arguments.of(
                setOf("absent1", "absent2", "employee"),
                "2021-12-01",
                "2021-12-31",
                EmployeesAvailabilityDTO(
                    "2021-12-01".toLocalDate(),
                    "2021-12-31".toLocalDate(),
                    listOf(
                        EmployeeAvailabilityDTO("absent1", leave("2021-12-13", "2021-12-17")),
                        EmployeeAvailabilityDTO("absent2", leave("2021-12-10", "2021-12-15")),
                        EmployeeAvailabilityDTO("employee", emptyList()),
                    ),
                ),
            ),
            // Leaves starting before the requested period are clipped to it
            Arguments.of(
                setOf("absent1", "absent2"),
                "2021-12-14",
                "2021-12-31",
                EmployeesAvailabilityDTO(
                    "2021-12-14".toLocalDate(),
                    "2021-12-31".toLocalDate(),
                    listOf(
                        EmployeeAvailabilityDTO("absent1", leave("2021-12-14", "2021-12-17")),
                        EmployeeAvailabilityDTO("absent2", leave("2021-12-14", "2021-12-15")),
                    ),
                ),
            ),
        )
    }

    private fun workingDays(): Stream<Arguments> =
        Stream.of(
            Arguments.of(
                "2021-01-01",
                "2021-01-31",
                15,
            ),
            Arguments.of(
                "2021-01-11",
                "2021-01-31",
                15,
            ),
            Arguments.of(
                "2021-01-01",
                "2021-01-10",
                0,
            ),
        )
    // </editor-fold>
}
