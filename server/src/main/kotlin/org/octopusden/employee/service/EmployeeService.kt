package org.octopusden.employee.service

import org.octopusden.employee.client.common.dto.Employee
import org.octopusden.employee.client.common.dto.RequiredTimeDTO
import java.time.LocalDate

interface EmployeeService {
    fun getEmployee(username: String): Employee
    fun getRequiredTime(username: String, fromDate: LocalDate, toDate: LocalDate): RequiredTimeDTO
    fun isUserAvailable(username: String): Boolean
    fun getEmployeeAvailableEarlier(employees: Set<String>): Employee
}
