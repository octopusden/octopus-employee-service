package org.octopusden.employee

import org.octopusden.employee.client.common.dto.Employee
import org.octopusden.employee.client.common.dto.RequiredTimeDTO
import org.octopusden.employee.client.common.exception.NotFoundException
import java.time.LocalDate

class EmployeeControllerTest : BaseEmployeeControllerTest() {

    override fun getRequiredTime(employee: String, fromDate: LocalDate, toDate: LocalDate): RequiredTimeDTO =
        client.getRequiredTime(employee, fromDate, toDate)

    override fun getEmployee(employee: String): Employee = client.getEmployee(employee)

    override fun getNotExistedEmployee(employee: String): String = try {
        client.getEmployee(employee)
        "Success response"
    } catch (e: NotFoundException) {
        e.message!!
    }

    override fun isEmployeeAvailable(employee: String): Boolean = client.isEmployeeAvailable(employee)

    companion object {
        private val client = TestFtUtils.getSecuredClient()
    }
}
