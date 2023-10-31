package org.octopusden.employee

import org.octopusden.employee.client.common.dto.Employee

class EmployeesControllerTest : BaseEmployeesControllerTest() {
    override fun getEmployeeAvailableEarlier(employees: Set<String>): Employee {
        return client.getEmployeeAvailableEarlier(employees)
    }

    companion object {
        private val client = TestFtUtils.getSecuredClient()
    }
}
