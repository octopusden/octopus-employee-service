package org.octopusden.employee

import org.octopusden.employee.client.common.dto.Employee
import org.octopusden.employee.client.common.dto.WorkingDaysDTO

class EmployeesControllerTest : BaseEmployeesControllerTest() {
    override fun getEmployeeAvailableEarlier(employees: Set<String>): Employee {
        return client.getEmployeeAvailableEarlier(employees)
    }

    override fun getWorkingDays(fromDate: String, toDate: String): WorkingDaysDTO {
        return client.getWorkingDays(fromDate.toLocalDate(), toDate.toLocalDate())
    }

    companion object {
        private val client = TestFtUtils.getSecuredClient()
    }
}
