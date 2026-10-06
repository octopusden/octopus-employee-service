package org.octopusden.employee

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
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
import java.util.stream.Stream

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

    @ParameterizedTest
    @MethodSource("invalidAvailabilityRequests")
    fun getAvailabilityWithInvalidRequest(
        employees: Set<String>,
        fromDate: String,
        toDate: String,
        expectedMessage: String,
    ) {
        val errorResponse = performGetAvailability(employees, LocalDate.parse(fromDate), LocalDate.parse(toDate))
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andReturn()
            .response
            .toObject(object : TypeReference<ErrorResponse>() {})
        Assertions.assertEquals(ErrorResponse(EmployeeServiceErrorCode.BAD_REQUEST, expectedMessage), errorResponse)
    }

    @Suppress("UnusedPrivateMember") // used by @MethodSource
    private fun invalidAvailabilityRequests(): Stream<Arguments> =
        Stream.of(
            Arguments.of(setOf("employee"), "2021-12-31", "2021-12-01", "fromDate '2021-12-31' must not be after toDate '2021-12-01'"),
            // 2021-01-01..2021-04-01 is 91 days
            Arguments.of(setOf("employee"), "2021-01-01", "2021-04-01", "Period must not exceed 90 days (fromDate and toDate included)"),
            Arguments.of((1..16).map { "employee$it" }.toSet(), "2021-12-01", "2021-12-31", "Number of employees must be between 1 and 15"),
            // no employees parameter at all
            Arguments.of(emptySet<String>(), "2021-12-01", "2021-12-31", "Number of employees must be between 1 and 15"),
            // ?employees=
            Arguments.of(setOf(""), "2021-12-01", "2021-12-31", "Number of employees must be between 1 and 15"),
            Arguments.of(setOf("employee", " "), "2021-12-01", "2021-12-31", "Employee usernames must not be blank"),
        )

    private fun performGetAvailability(
        employees: Set<String>,
        fromDate: LocalDate,
        toDate: LocalDate,
    ): ResultActions =
        mvc.perform(
            MockMvcRequestBuilders
                .get("/employees/availability")
                .apply { if (employees.isNotEmpty()) param("employees", *employees.toTypedArray()) }
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
