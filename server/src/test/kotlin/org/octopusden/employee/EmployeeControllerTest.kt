package org.octopusden.employee

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.`when`
import org.octopusden.employee.client.common.dto.Employee
import org.octopusden.employee.client.common.dto.ErrorResponse
import org.octopusden.employee.client.common.dto.ManagerDTO
import org.octopusden.employee.client.common.dto.RequiredTimeDTO
import org.octopusden.employee.client.common.exception.NotFoundException
import org.octopusden.employee.service.AdService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.time.LocalDate
import java.util.Locale

@AutoConfigureMockMvc
@ExtendWith(SpringExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(
    classes = [EmployeeServiceApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
@ActiveProfiles("test")
@WithMockUser(authorities = ["ROLE_EMPLOYEE_SERVICE_USER_DEV"])
class EmployeeControllerTest : BaseEmployeeControllerTest() {
    @Autowired
    private lateinit var mvc: MockMvc

    @Autowired
    private lateinit var mapper: ObjectMapper

    @MockBean
    private lateinit var adService: AdService

    @BeforeAll
    fun beforeAllRepositoryControllerTests() {
        mapper.setLocale(Locale.ENGLISH)
    }

    override fun getRequiredTime(
        employee: String,
        fromDate: LocalDate,
        toDate: LocalDate,
    ): RequiredTimeDTO =
        mvc
            .perform(
                MockMvcRequestBuilders
                    .get("/employee/{employee}/required-time", employee)
                    .param("fromDate", fromDate.format(isoLocalDateFormatter))
                    .param("toDate", toDate.format(isoLocalDateFormatter))
                    .accept(MediaType.APPLICATION_JSON),
            ).andExpect(MockMvcResultMatchers.status().is2xxSuccessful)
            .andReturn()
            .response
            .toObject(object : TypeReference<RequiredTimeDTO>() {})

    override fun getEmployee(employee: String): Employee =
        mvc
            .perform(
                MockMvcRequestBuilders
                    .get("/employee/{employee}", employee)
                    .accept(MediaType.APPLICATION_JSON),
            ).andExpect(MockMvcResultMatchers.status().is2xxSuccessful)
            .andReturn()
            .response
            .toObject(object : TypeReference<Employee>() {})

    override fun getNotExistedEmployee(employee: String): String =
        mvc
            .perform(
                MockMvcRequestBuilders
                    .get("/employee/{employee}", employee)
                    .accept(MediaType.APPLICATION_JSON),
            ).andExpect(MockMvcResultMatchers.status().is4xxClientError)
            .andReturn()
            .response
            .toObject(object : TypeReference<ErrorResponse>() {})
            .errorMessage

    override fun isEmployeeAvailable(employee: String): Boolean =
        mvc
            .perform(
                MockMvcRequestBuilders
                    .get("/employee/{employee}/available", employee)
                    .accept(MediaType.APPLICATION_JSON),
            ).andExpect(MockMvcResultMatchers.status().is2xxSuccessful)
            .andReturn()
            .response
            .contentAsString!!
            .toBoolean()

    @Test
    fun getManagerWithManager() {
        `when`(adService.getManager("employee")).thenReturn("manager")
        val result = mvc
            .perform(
                MockMvcRequestBuilders
                    .get("/employee/{username}/manager", "employee")
                    .accept(MediaType.APPLICATION_JSON),
            ).andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()
            .response
            .toObject(object : TypeReference<ManagerDTO>() {})
        Assertions.assertEquals(ManagerDTO("manager"), result)
    }

    @Test
    fun getManagerWithNoManager() {
        `when`(adService.getManager("employee")).thenReturn(null)
        val result = mvc
            .perform(
                MockMvcRequestBuilders
                    .get("/employee/{username}/manager", "employee")
                    .accept(MediaType.APPLICATION_JSON),
            ).andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()
            .response
            .toObject(object : TypeReference<ManagerDTO>() {})
        Assertions.assertEquals(ManagerDTO(null), result)
    }

    @Test
    fun getManagerUserNotFound() {
        doThrow(NotFoundException("User 'nonexistent' not found in AD"))
            .`when`(adService)
            .getManager("nonexistent")
        mvc
            .perform(
                MockMvcRequestBuilders
                    .get("/employee/{username}/manager", "nonexistent")
                    .accept(MediaType.APPLICATION_JSON),
            ).andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    private fun <T> MockHttpServletResponse.toObject(typeReference: TypeReference<T>): T =
        mapper.readValue(this.contentAsByteArray, typeReference)
}
