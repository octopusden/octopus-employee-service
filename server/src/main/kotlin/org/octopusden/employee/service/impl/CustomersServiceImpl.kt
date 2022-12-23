package org.octopusden.employee.service.impl

import org.octopusden.employee.service.CustomersService
import org.octopusden.employee.client.common.dto.CustomerDTO
import org.octopusden.employee.service.jira.client.jira2.Jira2Client
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class CustomersServiceImpl(private val jira2Client: Jira2Client) : CustomersService {
    override fun getCustomers() : Set<CustomerDTO> {
        log.info("Get All Customers")
        return jira2Client.getCustomers()
    }

    companion object {
        private val log = LoggerFactory.getLogger(CustomersServiceImpl::class.java)
    }
}
