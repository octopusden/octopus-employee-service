package org.octopusden.employee.service

import org.octopusden.employee.client.common.dto.CustomerDTO

interface CustomersService {
    fun getCustomers(): Set<CustomerDTO>
}
