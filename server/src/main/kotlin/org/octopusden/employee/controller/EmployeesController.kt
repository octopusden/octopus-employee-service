package org.octopusden.employee.controller

import org.octopusden.employee.client.common.dto.Employee
import org.octopusden.employee.service.EmployeeService
import org.springframework.security.access.annotation.Secured
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("employees")
@Secured("ROLE_ADMIN", "ROLE_EMPLOYEE_SERVICE_TECHNICAL_USER")
class EmployeesController(private val employeeService: EmployeeService) {
    @GetMapping("available-earlier")
    fun getEmployeeAvailableEarlier(@RequestParam employees: Set<String>): Employee = employeeService.getEmployeeAvailableEarlier(employees)
}
