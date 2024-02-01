package org.octopusden.employee.controller

import org.octopusden.employee.client.common.dto.Employee
import org.octopusden.employee.client.common.dto.WorkingDaysDTO
import org.octopusden.employee.service.EmployeeService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("employees")
class EmployeesController(private val employeeService: EmployeeService) {
    @GetMapping("available-earlier")
    @PreAuthorize("@employeeServicePermissionEvaluator.hasPermission('ACCESS_EMPLOYEE')")
    fun getEmployeeAvailableEarlier(@RequestParam employees: Set<String>): Employee = employeeService.getEmployeeAvailableEarlier(employees)

    @GetMapping("working-days")
    @PreAuthorize("@employeeServicePermissionEvaluator.hasPermission('ACCESS_EMPLOYEE')")
    fun getWorkingDays(
        @RequestParam("fromDate", required = true) fromDate: LocalDate,
        @RequestParam("toDate", required = true) toDate: LocalDate
    ): WorkingDaysDTO = employeeService.getWorkingDays(fromDate, toDate)
}
