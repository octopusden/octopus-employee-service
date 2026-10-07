package org.octopusden.employee

import org.octopusden.employee.client.common.dto.Employee
import org.octopusden.employee.client.common.dto.EmployeesAvailabilityDTO
import org.octopusden.employee.client.common.dto.WorkingDaysDTO
import java.time.LocalDate

class EmployeesControllerTest : BaseEmployeesControllerTest() {
    override fun getEmployeeAvailableEarlier(employees: Set<String>): Employee = client.getEmployeeAvailableEarlier(employees)

    override fun getAvailability(
        employees: Set<String>,
        fromDate: LocalDate,
        toDate: LocalDate,
    ): EmployeesAvailabilityDTO = client.getAvailability(employees, fromDate, toDate)

    override fun getWorkingDays(
        fromDate: String,
        toDate: String,
    ): WorkingDaysDTO = client.getWorkingDays(fromDate.toLocalDate(), toDate.toLocalDate())

    companion object {
        private val client = TestFtUtils.getSecuredClient()
    }
}
