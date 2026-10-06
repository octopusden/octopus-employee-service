package org.octopusden.employee.client.common.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.LocalDate

@JsonIgnoreProperties(ignoreUnknown = true)
data class EmployeesAvailabilityDTO(
    val from: LocalDate,
    val to: LocalDate,
    val employees: List<EmployeeAvailabilityDTO>,
)
