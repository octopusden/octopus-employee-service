package org.octopusden.employee.service.impl

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.octopusden.employee.client.common.dto.ManagerDTO
import org.octopusden.employee.client.common.exception.NotFoundException
import org.octopusden.employee.config.EmployeeServiceProperties
import org.octopusden.employee.service.AdService
import org.octopusden.employee.service.OneCService
import org.octopusden.employee.service.jira.client.common.JiraClientException
import org.octopusden.employee.service.jira.client.common.JiraUser
import org.octopusden.employee.service.jira.client.jira1.Jira1Client
import org.octopusden.employee.service.jira.client.jira2.Jira2Client
import org.springframework.beans.factory.ObjectProvider

class EmployeeServiceImplGetManagerTest {

    private val jira1Client = Mockito.mock(Jira1Client::class.java)
    private val adServiceProvider = Mockito.mock(ObjectProvider::class.java) as ObjectProvider<AdService>
    private val employeeService = EmployeeServiceImpl(
        Mockito.mock(OneCService::class.java),
        jira1Client,
        Mockito.mock(Jira2Client::class.java),
        EmployeeServiceProperties(8, EmployeeServiceProperties.UserAvailability("")),
        adServiceProvider,
    )

    @Test
    fun getManagerReturnsNullWhenAdIsNotConfiguredAndUserExists() {
        Mockito.`when`(adServiceProvider.getIfAvailable()).thenReturn(null)
        Mockito.`when`(jira1Client.getUser("employee")).thenReturn(JiraUser("employee", "employee@example.local", "Employee", true))

        Assertions.assertEquals(ManagerDTO(null), employeeService.getManager("employee"))
    }

    @Test
    fun getManagerThrowsNotFoundWhenAdIsNotConfiguredAndUserDoesNotExist() {
        Mockito.`when`(adServiceProvider.getIfAvailable()).thenReturn(null)
        Mockito.`when`(jira1Client.getUser("nobody")).thenThrow(JiraClientException(404, "not found"))

        Assertions.assertThrows(NotFoundException::class.java) { employeeService.getManager("nobody") }
    }

    @Test
    fun getManagerDelegatesToAdServiceWhenConfigured() {
        val adService = Mockito.mock(AdService::class.java)
        Mockito.`when`(adServiceProvider.getIfAvailable()).thenReturn(adService)
        Mockito.`when`(adService.getManager("employee")).thenReturn("managerSam")

        Assertions.assertEquals(ManagerDTO("managerSam"), employeeService.getManager("employee"))
        Mockito.verifyNoInteractions(jira1Client)
    }
}
