package org.octopusden.employee

import org.octopusden.employee.client.EmployeeServiceClient
import org.octopusden.employee.client.common.dto.RequiredTimeDTO
import feign.FeignException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.LocalDate

private const val EXISTED_EMPLOYEE = "employee"

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EmployeeControllerSecurityTest {

    private val securedClient = TestFtUtils.getSecuredClient()
    private val unsecuredClient = TestFtUtils.getUnsecuredClient()

    @Test
    fun getRequiredTimeByAuthorizedUserTest() {
        val requiredTimeDTO = getRequiredTime(securedClient)
        Assertions.assertEquals(EXISTED_EMPLOYEE, requiredTimeDTO.employee.username)
    }

    @Test
    fun getRequiredTimeByUnauthorizedTest() {
        Assertions.assertThrows(FeignException.Unauthorized::class.java) {
            getRequiredTime(unsecuredClient)
        }
    }

    private fun getRequiredTime(client: EmployeeServiceClient): RequiredTimeDTO =
        client.getRequiredTime(EXISTED_EMPLOYEE, LocalDate.parse("2021-01-01"), LocalDate.parse("2021-01-31"))
}
