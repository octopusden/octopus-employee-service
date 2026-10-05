package org.octopusden.employee

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.octopusden.employee.client.common.dto.Employee
import org.octopusden.employee.client.common.dto.EmployeeServiceErrorCode
import org.octopusden.employee.client.common.dto.EmployeesAvailabilityDTO
import org.octopusden.employee.client.common.dto.ErrorResponse
import org.octopusden.employee.client.common.dto.WorkingDaysDTO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
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
class EmployeesControllerTest : BaseEmployeesControllerTest() {
    @Autowired
    private lateinit var mvc: MockMvc

    @Autowired
    private lateinit var mapper: ObjectMapper

    @BeforeAll
    fun beforeAllRepositoryControllerTests() {
        mapper.setLocale(Locale.ENGLISH)
    }

    override fun getEmployeeAvailableEarlier(employees: Set<String>): Employee =
        mvc
            .perform(
                MockMvcRequestBuilders
                    .get("/employees/available-earlier")
                    .param("employees", *employees.toTypedArray())
                    .accept(MediaType.APPLICATION_JSON),
            ).andExpect(MockMvcResultMatchers.status().is2xxSuccessful)
            .andReturn()
            .response
            .toObject(object : TypeReference<Employee>() {})

    override fun getAvailability(
        employees: Set<String>,
        fromDate: LocalDate,
        toDate: LocalDate,
    ): EmployeesAvailabilityDTO =
        performGetAvailability(employees, fromDate, toDate)
            .andExpect(MockMvcResultMatchers.status().is2xxSuccessful)
            .andReturn()
            .response
            .toObject(object : TypeReference<EmployeesAvailabilityDTO>() {})

    @Test
    fun getAvailabilityOfNotExistedEmployee() {
        performGetAvailability(setOf("nonexistent"), LocalDate.parse("2021-12-01"), LocalDate.parse("2021-12-31"))
            .andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    fun getAvailabilityWithFromDateAfterToDate() {
        val errorResponse = performGetAvailability(setOf("employee"), LocalDate.parse("2021-12-31"), LocalDate.parse("2021-12-01"))
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andReturn()
            .response
            .toObject(object : TypeReference<ErrorResponse>() {})
        Assertions.assertEquals(
            ErrorResponse(EmployeeServiceErrorCode.BAD_REQUEST, "fromDate '2021-12-31' must not be after toDate '2021-12-01'"),
            errorResponse,
        )
    }

    private fun performGetAvailability(
        employees: Set<String>,
        fromDate: LocalDate,
        toDate: LocalDate,
    ): ResultActions =
        mvc.perform(
            MockMvcRequestBuilders
                .get("/employees/availability")
                .param("employees", *employees.toTypedArray())
                .param("fromDate", fromDate.format(isoLocalDateFormatter))
                .param("toDate", toDate.format(isoLocalDateFormatter))
                .accept(MediaType.APPLICATION_JSON),
        )

    override fun getWorkingDays(
        fromDate: String,
        toDate: String,
    ): WorkingDaysDTO =
        mvc
            .perform(
                MockMvcRequestBuilders
                    .get("/employees/working-days")
                    .param("fromDate", fromDate.format(isoLocalDateFormatter))
                    .param("toDate", toDate.format(isoLocalDateFormatter))
                    .accept(MediaType.APPLICATION_JSON),
            ).andExpect(MockMvcResultMatchers.status().is2xxSuccessful)
            .andReturn()
            .response
            .toObject(object : TypeReference<WorkingDaysDTO>() {})

    private fun <T> MockHttpServletResponse.toObject(typeReference: TypeReference<T>): T =
        mapper.readValue(this.contentAsByteArray, typeReference)
}
