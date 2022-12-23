package org.octopusden.employee.controller

import org.octopusden.employee.service.CustomersService
import org.octopusden.employee.client.common.dto.CustomerDTO
import org.springframework.http.MediaType
import org.springframework.security.access.annotation.Secured
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("customers")
@Secured("ROLE_ADMIN", "ROLE_EMPLOYEE_SERVICE_TECHNICAL_USER")
class CustomersController(private val customersService: CustomersService) {
    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getCustomers(): Set<CustomerDTO> = customersService.getCustomers()
}
