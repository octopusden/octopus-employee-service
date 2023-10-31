package org.octopusden.employee

import org.octopusden.employee.client.common.dto.Employee
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class BaseEmployeesControllerTest : BaseTest() {
    @ParameterizedTest
    @MethodSource("availableEarlier")
    fun getEmployeeAvailableEarlierTest(employees: Set<String>, expectedEmployee: Employee) {
        val employeeAvailableEarlier = getEmployeeAvailableEarlier(employees)
        Assertions.assertEquals(expectedEmployee, employeeAvailableEarlier)
    }

    protected abstract fun getEmployeeAvailableEarlier(employees: Set<String>): Employee

    //<editor-fold defaultstate="collapsed" desc="test data">
    private fun availableEarlier(): Stream<Arguments> = Stream.of(
        Arguments.of(
            setOf("absent1", "absent2"), Employee("absent2", true)
        ),
        Arguments.of(
            setOf("absent1", "absent2", "employee"), Employee("employee", true)
        )
    )
    //</editor-fold>
}
