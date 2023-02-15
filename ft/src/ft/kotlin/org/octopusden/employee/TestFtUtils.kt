package org.octopusden.employee

import org.octopusden.employee.client.EmployeeServiceClient
import org.octopusden.employee.client.impl.ClassicEmployeeServiceClient
import org.octopusden.employee.client.impl.EmployeeServiceClientParametersProvider

private const val FAKE_BEARER = "fake.bearer"
private const val API_URL = "http://localhost:8765/employee-service"
private const val RETRY_IN_MILLIS = 180000

class TestFtUtils private constructor() {
    companion object {
        @JvmStatic
        fun getSecuredClient(): EmployeeServiceClient =
            ClassicEmployeeServiceClient(object : EmployeeServiceClientParametersProvider {
                override fun getBearerToken(): String? = null
                override fun getApiUrl() = API_URL
                override fun getTimeRetryInMillis() = RETRY_IN_MILLIS
                override fun getBasicCredentials(): String = "${System.getProperty("employee-service.user")}:${System.getProperty("employee-service.password")}"
            })

        @JvmStatic
        fun getUnsecuredClient(): EmployeeServiceClient =
            ClassicEmployeeServiceClient(object : EmployeeServiceClientParametersProvider {
                override fun getBearerToken(): String = FAKE_BEARER
                override fun getApiUrl() = API_URL
                override fun getTimeRetryInMillis() = RETRY_IN_MILLIS
                override fun getBasicCredentials(): String? = null
            })
    }
}
