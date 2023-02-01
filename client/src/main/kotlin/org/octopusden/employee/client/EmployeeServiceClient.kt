package org.octopusden.employee.client

import org.octopusden.employee.client.common.dto.CustomerDTO
import org.octopusden.employee.client.common.dto.Employee
import org.octopusden.employee.client.common.dto.RequiredTimeDTO
import org.octopusden.employee.client.common.dto.ServerInfo
import org.octopusden.employee.client.common.exception.NotFoundException
import org.octopusden.employee.client.common.feign.LocalDateExpander
import feign.CollectionFormat
import feign.Param
import feign.QueryMap
import feign.RequestLine
import org.octopusden.employee.client.common.dto.Health
import java.time.LocalDate

interface EmployeeServiceClient {

    @RequestLine("GET actuator/info")
    fun getServerInfo(): ServerInfo

    @Throws(NotFoundException::class)
    @RequestLine("GET employee/{employee}")
    fun getEmployee(@Param("employee") employee: String): Employee

    @Throws(NotFoundException::class)
    @RequestLine("GET employee/{employee}/required-time?employee={employee}&fromDate={fromDate}&toDate={toDate}")
    fun getRequiredTime(
        @Param("employee") employee: String,
        @Param("fromDate", expander = LocalDateExpander::class) fromDate: LocalDate,
        @Param("toDate", expander = LocalDateExpander::class) toDate: LocalDate
    ): RequiredTimeDTO

    @Throws(NotFoundException::class)
    @RequestLine("GET employee/{employee}/available")
    fun isEmployeeAvailable(@Param("employee") employee: String): Boolean

    @Throws(NotFoundException::class)
    @RequestLine("GET employees/available-earlier?employees={employees}", collectionFormat = CollectionFormat.CSV)
    fun getEmployeeAvailableEarlier(@Param("employees") @QueryMap employees: List<String>): Employee

    @RequestLine("GET customers")
    fun getCustomers(): Set<CustomerDTO>

    @RequestLine("GET actuator/health/oneC")
    fun oneCHealth(): Health
}
