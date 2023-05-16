package org.octopusden.employee

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultMatcher
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@AutoConfigureMockMvc
@ExtendWith(SpringExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(
    classes = [EmployeeServiceApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("test")
class EmployeeControllerSecurityTest {

    @Autowired
    private lateinit var mvc: MockMvc

    @Test
    fun getRequiredTimeByUnauthorizedTest() {
        getRequiredTimeRequest(MockMvcResultMatchers.status().`is`(HttpStatus.UNAUTHORIZED.value()))
    }

    @Test
    @WithMockUser(authorities = ["ROLE_EMPLOYEE_SERVICE_USER"])
    fun getRequiredTimeByAuthorizedUserNotInGroupTest() {
        getRequiredTimeRequest(MockMvcResultMatchers.status().`is`(HttpStatus.FORBIDDEN.value()))
    }

    @Test
    @WithMockUser(authorities = ["EMPLOYEE_SERVICE_USER_DEV"])
    fun getRequiredTimeByAuthorizedUserTest() {
        getRequiredTimeRequest(MockMvcResultMatchers.status().is2xxSuccessful)
    }

    private fun getRequiredTimeRequest(expectedStatus: ResultMatcher) {
        mvc.perform(
            MockMvcRequestBuilders.get("/employee/{employee}/required-time", "employee")
                .param("fromDate", "2021-01-01")
                .param("toDate", "2021-01-31")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(expectedStatus)
    }
}
