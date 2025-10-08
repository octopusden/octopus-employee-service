package org.octopusden.employee

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import feign.Feign
import feign.jackson.JacksonDecoder
import org.octopusden.employee.client.EmployeeServiceClient
import org.octopusden.employee.client.impl.ClassicEmployeeServiceClient
import org.octopusden.employee.client.impl.EmployeeServiceClientParametersProvider

private const val FAKE_BEARER = "fake.bearer"
private const val RETRY_IN_MILLIS = 180000

class TestFtUtils private constructor() {
    companion object {
        private val hostApiGateway = System.getProperty("test.api-gateway-host")
            ?: throw Exception("System property 'test.api-gateway-host' must be defined")
        private val apiUrl = "http://${hostApiGateway}/employee-service"

        @JvmStatic
        fun getSecuredClient(): EmployeeServiceClient =
            ClassicEmployeeServiceClient(object : EmployeeServiceClientParametersProvider {
                override fun getBearerToken(): String? = null
                override fun getApiUrl() = apiUrl
                override fun getTimeRetryInMillis() = RETRY_IN_MILLIS
                override fun getBasicCredentials(): String =
                    "${System.getProperty("employee-service.user")}:${System.getProperty("employee-service.password")}"
            })

        @JvmStatic
        fun getFakeTokenClient(): EmployeeServiceClient =
            ClassicEmployeeServiceClient(object : EmployeeServiceClientParametersProvider {
                override fun getBearerToken(): String = FAKE_BEARER
                override fun getApiUrl() = apiUrl
                override fun getTimeRetryInMillis() = RETRY_IN_MILLIS
                override fun getBasicCredentials(): String? = null
            })

        @JvmStatic
        fun getUnsecuredClient(): EmployeeServiceClient =
            Feign.builder()
                .decoder(JacksonDecoder(with(jacksonObjectMapper()) {
                    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    this
                }))
                .target(EmployeeServiceClient::class.java, apiUrl)
    }
}
