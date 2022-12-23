package org.octopusden.employee

import org.octopusden.employee.client.common.dto.Employee
import org.octopusden.employee.client.common.dto.RequiredTimeDTO
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.time.LocalDate
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class BaseEmployeeControllerTest : BaseTest() {

    @ParameterizedTest
    @MethodSource("requiredTime")
    fun getRequiredTimeTest(employee: String, dateFrom: String, dateTo: String, expectedTime: Int) {
        val requiredTimeDTO = getRequiredTime(
            employee,
            dateFrom.toLocalDate(),
            dateTo.toLocalDate()
        )
        Assertions.assertEquals(expectedTime, requiredTimeDTO.requiredHours)
    }

    @ParameterizedTest
    @MethodSource("existedEmployees")
    fun getExistedEmployeeTest(username: String, active: Boolean) {
        val actual = getEmployee(username)
        Assertions.assertEquals(Employee(username, active), actual)
    }

    @Test
    fun getNotExistedEmployeeTest() {
        val employee = "nonexistent"
        val errorMessage = getNotExistedEmployee(employee)
        Assertions.assertEquals("The user named '$employee' does not exist", errorMessage)
    }

    @ParameterizedTest
    @MethodSource("availableEmployees")
    fun isEmployeeAvailableTest(username: String, expectedAvailable: Boolean) {
        Assertions.assertEquals(expectedAvailable, isEmployeeAvailable(username))
    }

    protected abstract fun getRequiredTime(employee: String, fromDate: LocalDate, toDate: LocalDate): RequiredTimeDTO
    protected abstract fun getEmployee(employee: String): Employee
    protected abstract fun getNotExistedEmployee(employee: String): String
    protected abstract fun isEmployeeAvailable(employee: String): Boolean

    //<editor-fold defaultstate="collapsed" desc="test data">
    private fun requiredTime(): Stream<Arguments> {
        return Stream.of(
            Arguments.of(
                "employee",
                "2021-01-01",
                "2021-01-31",
                112
            ), Arguments.of(
                "employee",
                "2021-01-11",
                "2021-01-31",
                112
            ), Arguments.of(
                "employee",
                "2021-01-01",
                "2021-01-10",
                0
            )
        )
    }

    private fun existedEmployees(): Stream<Arguments> = Stream.of(
        Arguments.of("employee", true),
        Arguments.of("inactive", false)
    )

    private fun availableEmployees(): Stream<Arguments> = Stream.of(
        Arguments.of("employee", true),
        Arguments.of("unavailable", false)
    )
    //</editor-fold>
}
