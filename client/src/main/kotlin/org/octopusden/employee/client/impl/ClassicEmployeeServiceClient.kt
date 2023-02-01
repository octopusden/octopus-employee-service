package org.octopusden.employee.client.impl

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.octopusden.employee.client.EmployeeServiceClient
import org.octopusden.employee.client.EmployeeServiceErrorDecoder
import org.octopusden.employee.client.EmployeeServiceRetry
import org.octopusden.employee.client.common.dto.CustomerDTO
import org.octopusden.employee.client.common.dto.Employee
import org.octopusden.employee.client.common.dto.RequiredTimeDTO
import org.octopusden.employee.client.common.dto.ServerInfo
import feign.Feign
import feign.Logger
import feign.Request
import feign.httpclient.ApacheHttpClient
import feign.jackson.JacksonDecoder
import feign.jackson.JacksonEncoder
import feign.slf4j.Slf4jLogger
import org.octopusden.employee.client.common.dto.Health
import java.nio.charset.Charset
import java.time.LocalDate
import java.util.Base64
import java.util.concurrent.TimeUnit

class ClassicEmployeeServiceClient(
    parametersProvider: EmployeeServiceClientParametersProvider,
    private val mapper: ObjectMapper,
) : EmployeeServiceClient {
    private var client =
        createClient(parametersProvider,
            mapper)

    constructor(parametersProvider: EmployeeServiceClientParametersProvider) : this(
        parametersProvider,
        getMapper()
    )

    override fun getServerInfo(): ServerInfo = client.getServerInfo()

    override fun getRequiredTime(employee: String, fromDate: LocalDate, toDate: LocalDate): RequiredTimeDTO =
        client.getRequiredTime(employee, fromDate, toDate)

    override fun isEmployeeAvailable(employee: String): Boolean = client.isEmployeeAvailable(employee)

    override fun getEmployee(employee: String): Employee = client.getEmployee(employee)

    override fun getEmployeeAvailableEarlier(employees: List<String>): Employee =
        client.getEmployeeAvailableEarlier(employees)

    override fun getCustomers(): Set<CustomerDTO> = client.getCustomers()

    fun updateApiParameters(apiParametersProvider: EmployeeServiceClientParametersProvider) {
        client = createClient(apiParametersProvider, mapper)
    }

    override fun oneCHealth(): Health = client.oneCHealth()

    companion object {
        private val base64Encoder = Base64.getEncoder()
        private fun getMapper(): ObjectMapper {
            val objectMapper = jacksonObjectMapper()
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            return objectMapper
        }

        private fun createClient(
            parametersProvider: EmployeeServiceClientParametersProvider,
            objectMapper: ObjectMapper,
        ): EmployeeServiceClient {
            return Feign.builder()
                .client(ApacheHttpClient())
                .options(Request.Options(5, TimeUnit.MINUTES, 5, TimeUnit.MINUTES, true))
                .encoder(JacksonEncoder(objectMapper))
                .decoder(JacksonDecoder(objectMapper))
                .errorDecoder(EmployeeServiceErrorDecoder(objectMapper))
                .retryer(EmployeeServiceRetry(parametersProvider.getTimeRetryInMillis()))
                .requestInterceptor { requestTemplate ->
                    val authHeader = parametersProvider.getBearerToken()
                        ?.let { token ->
                            if (token.isNotBlank()) {
                                "Bearer $token"
                            } else {
                                null
                            }
                        }
                        ?: parametersProvider.getBasicCredentials()
                            ?.let { basicCredentials ->
                                if (basicCredentials.replace(":", "").isNotBlank()) {
                                    "Basic ${
                                        base64Encoder.encodeToString(basicCredentials.toByteArray(Charset.forName(Charsets.UTF_8.name())))
                                    }"
                                } else {
                                    null
                                }
                            }
                        ?: throw IllegalArgumentException("Bearer token or basic credentials must be provided")
                    requestTemplate.header("Authorization", authHeader)
                }
                .logger(Slf4jLogger(EmployeeServiceClient::class.java))
                .logLevel(Logger.Level.FULL)
                .target(EmployeeServiceClient::class.java, parametersProvider.getApiUrl())
        }
    }
}
