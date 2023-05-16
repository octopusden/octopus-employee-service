package org.octopusden.employee.controller

import org.octopusden.employee.service.CustomersService
import org.octopusden.employee.client.common.dto.CustomerDTO
import org.springframework.http.MediaType
import org.springframework.security.access.annotation.Secured
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("customers")
class CustomersController(private val customersService: CustomersService) {
    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("@employeeServicePermissionEvaluator.hasPermission('ACCESS_CUSTOMER')")
    fun getCustomers(): Set<CustomerDTO> = customersService.getCustomers()
}
