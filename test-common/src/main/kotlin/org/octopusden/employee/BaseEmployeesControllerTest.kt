package org.octopusden.employee

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.octopusden.employee.client.common.dto.Employee
import org.octopusden.employee.client.common.dto.WorkingDaysDTO
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class BaseEmployeesControllerTest : BaseTest() {
    @ParameterizedTest
    @MethodSource("availableEarlier")
    fun getEmployeeAvailableEarlierTest(employees: Set<String>, expectedEmployee: Employee) {
        val employeeAvailableEarlier = getEmployeeAvailableEarlier(employees)
        Assertions.assertEquals(expectedEmployee, employeeAvailableEarlier)
    }

    @ParameterizedTest
    @MethodSource("workingDays")
    fun getWorkingDaysTest(dateFrom: String, dateTo: String, expectedWorkingDays: Int) {
        val workingDays = getWorkingDays(dateFrom, dateTo)
        Assertions.assertEquals(expectedWorkingDays, workingDays.workingDays)

    }

    protected abstract fun getEmployeeAvailableEarlier(employees: Set<String>): Employee

    protected abstract fun getWorkingDays(fromDate: String, toDate: String): WorkingDaysDTO

    //<editor-fold defaultstate="collapsed" desc="test data">
    private fun availableEarlier(): Stream<Arguments> = Stream.of(
        Arguments.of(
            setOf("absent1", "absent2"), Employee("absent2", true)
        ),
        Arguments.of(
            setOf("absent1", "absent2", "employee"), Employee("employee", true)
        )
    )

    private fun workingDays(): Stream<Arguments> {
        return Stream.of(
            Arguments.of(
                "2021-01-01",
                "2021-01-31",
                15
            ), Arguments.of(
                "2021-01-11",
                "2021-01-31",
                15
            ), Arguments.of(
                "2021-01-01",
                "2021-01-10",
                0
            )
        )
    }
    //</editor-fold>
}
