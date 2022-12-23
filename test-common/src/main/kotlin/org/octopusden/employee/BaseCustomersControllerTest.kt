package org.octopusden.employee

import org.octopusden.employee.client.common.dto.CustomerDTO
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class BaseCustomersControllerTest {

    @Test
    fun getCustomersTest() {
        val customers = getCustomers()
        val expectedCustomers = listOf(
            CustomerDTO(1, "Customer1", true),
            CustomerDTO(2, "Customer2", false)
        )
        Assertions.assertIterableEquals(expectedCustomers, customers.toList().sortedBy { it.instanceId })
    }

    abstract fun getCustomers(): Set<CustomerDTO>
}
