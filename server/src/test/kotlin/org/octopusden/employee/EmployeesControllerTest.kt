package org.octopusden.employee

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.octopusden.employee.client.common.dto.Employee
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.util.Locale

@AutoConfigureMockMvc
@ExtendWith(SpringExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(
    classes = [EmployeeServiceApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
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

    override fun getEmployeeAvailableEarlier(employees: Set<String>): Employee = mvc.perform(
        MockMvcRequestBuilders.get("/employees/available-earlier")
            .param("employees", *employees.toTypedArray())
            .accept(MediaType.APPLICATION_JSON)
    )
        .andExpect(MockMvcResultMatchers.status().is2xxSuccessful)
        .andReturn()
        .response
        .toObject(object : TypeReference<Employee>() {})

    override fun getWorkingDays(fromDate: String, toDate: String): WorkingDaysDTO = mvc.perform(
        MockMvcRequestBuilders.get("/employees/working-days")
            .param("fromDate", fromDate.format(isoLocalDateFormatter))
            .param("toDate", toDate.format(isoLocalDateFormatter))
            .accept(MediaType.APPLICATION_JSON)
    )
        .andExpect(MockMvcResultMatchers.status().is2xxSuccessful)
        .andReturn()
        .response
        .toObject(object : TypeReference<WorkingDaysDTO>() {})

    private fun <T> MockHttpServletResponse.toObject(typeReference: TypeReference<T>): T =
        mapper.readValue(this.contentAsByteArray, typeReference)
}
