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
import org.octopusden.employee.config.OneCProperties

@Service
@EnableConfigurationProperties(EmployeeServiceProperties::class)
class OneCServiceImpl(
    private val oneCClient: OneCClient,
    private val employeeServiceProperties: EmployeeServiceProperties,
    private val oneCProperties: OneCProperties
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

    override fun isRequiredTimeValid(): Boolean {
        return try {
            oneCProperties.health?.let { health ->
                val result = oneCClient.getPlannedTime(health.user, health.startDate, health.endDate)
                if (result.isEmpty()) {
                    throw IllegalArgumentException("No required time found for user ${health.user}")
                }
                true
            } ?: false
        } catch (e: Exception) {
            throw IllegalStateException("Required time is not valid", e)
        }
    }
}
