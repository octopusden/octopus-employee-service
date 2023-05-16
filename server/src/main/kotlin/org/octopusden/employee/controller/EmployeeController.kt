package org.octopusden.employee.controller

import org.octopusden.employee.client.common.dto.Employee
import org.octopusden.employee.client.common.dto.RequiredTimeDTO
import org.octopusden.employee.service.EmployeeService
import org.springframework.security.access.annotation.Secured
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("employee")
class EmployeeController(
    private val employeeService: EmployeeService,
) {
    @GetMapping("{username}")
    @PreAuthorize("@employeeServicePermissionEvaluator.hasPermission('ACCESS_EMPLOYEE')")
    fun getEmployee(@PathVariable("username") username: String): Employee = employeeService.getEmployee(username)

    @GetMapping("{username}/required-time")
    @PreAuthorize("@employeeServicePermissionEvaluator.hasPermission('ACCESS_EMPLOYEE')")
    fun getRequiredTime(
        @PathVariable("username") username: String,
        @RequestParam("fromDate", required = true) fromDate: LocalDate,
        @RequestParam("toDate", required = true) toDate: LocalDate
    ): RequiredTimeDTO {
        return employeeService.getRequiredTime(username, fromDate, toDate)
    }

    @GetMapping("{username}/available")
    @PreAuthorize("@employeeServicePermissionEvaluator.hasPermission('ACCESS_EMPLOYEE')")
    fun isEmployeeAvailable(@PathVariable("username") username: String): Boolean = employeeService.isUserAvailable(username)
}
