package org.octopusden.employee.service.impl

import org.octopusden.employee.client.common.dto.Employee
import org.octopusden.employee.client.common.dto.RequiredTimeDTO
import org.octopusden.employee.client.common.dto.WorkingDaysDTO
import org.octopusden.employee.config.EmployeeServiceProperties
import org.octopusden.employee.service.OneCService
import org.octopusden.employee.service.onec.client.OneCClient
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate

@Service
@EnableConfigurationProperties(EmployeeServiceProperties::class)
class OneCServiceImpl(
    private val oneCClient: OneCClient,
    private val employeeServiceProperties: EmployeeServiceProperties
) : OneCService {

    override fun getRequiredTime(employee: Employee, fromDate: LocalDate, toDate: LocalDate): RequiredTimeDTO {
        val requiredDays = oneCClient.getPlannedTime(employee.username, fromDate, toDate)
            .map { it.md }.fold(BigDecimal(0)) { acc, next ->
            acc.plus(next)
        }
        return RequiredTimeDTO(
            employee,
            requiredDays.multiply(employeeServiceProperties.workDayHours.toBigDecimal()).toInt()
        )
    }

    override fun getWorkingDays(fromDate: LocalDate, toDate: LocalDate): WorkingDaysDTO {
        return oneCClient.getWorkingDays(fromDate, toDate)
    }
}
