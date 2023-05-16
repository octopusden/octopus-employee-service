package org.octopusden.employee

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.octopusden.employee.client.common.dto.Employee
import org.octopusden.employee.client.common.dto.ErrorResponse
import org.octopusden.employee.client.common.dto.RequiredTimeDTO
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
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
import java.time.LocalDate
import java.util.Locale

@AutoConfigureMockMvc
@ExtendWith(SpringExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(
    classes = [EmployeeServiceApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("test")
@WithMockUser(authorities = ["EMPLOYEE_SERVICE_USER_DEV"])
class EmployeeControllerTest : BaseEmployeeControllerTest() {

    @Autowired
    private lateinit var mvc: MockMvc

    @Autowired
    private lateinit var mapper: ObjectMapper

    @BeforeAll
    fun beforeAllRepositoryControllerTests() {
        mapper.setLocale(Locale.ENGLISH)
    }

    override fun getRequiredTime(employee: String, fromDate: LocalDate, toDate: LocalDate): RequiredTimeDTO =
        mvc.perform(
            MockMvcRequestBuilders.get("/employee/{employee}/required-time", employee)
                .param("fromDate", fromDate.format(isoLocalDateFormatter))
                .param("toDate", toDate.format(isoLocalDateFormatter))
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().is2xxSuccessful)
            .andReturn()
            .response
            .toObject(object : TypeReference<RequiredTimeDTO>() {})

    override fun getEmployee(employee: String): Employee =
        mvc.perform(
            MockMvcRequestBuilders.get("/employee/{employee}", employee)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().is2xxSuccessful)
            .andReturn()
            .response
            .toObject(object : TypeReference<Employee>() {})


    override fun getNotExistedEmployee(employee: String): String =
        mvc.perform(
            MockMvcRequestBuilders.get("/employee/{employee}", employee)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().is4xxClientError)
            .andReturn()
            .response
            .toObject(object : TypeReference<ErrorResponse>() {})
            .errorMessage

    override fun isEmployeeAvailable(employee: String): Boolean =
        mvc.perform(
            MockMvcRequestBuilders.get("/employee/{employee}/available", employee)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().is2xxSuccessful)
            .andReturn()
            .response
            .contentAsString!!
            .toBoolean()

    private fun <T> MockHttpServletResponse.toObject(typeReference: TypeReference<T>): T =
        mapper.readValue(this.contentAsByteArray, typeReference)
}
