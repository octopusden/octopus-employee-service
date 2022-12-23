package org.octopusden.employee

import org.octopusden.employee.client.common.dto.CustomerDTO

class CustomersControllerTest : org.octopusden.employee.BaseCustomersControllerTest() {
    override fun getCustomers(): Set<CustomerDTO> = org.octopusden.employee.CustomersControllerTest.Companion.client.getCustomers()

    companion object {
        private val client = org.octopusden.employee.TestFtUtils.Companion.getSecuredClient()
    }
}
