package org.octopusden.employee.client.common.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class EmployeesAvailabilityDTO(
    val from: String,
    val to: String,
    val employees: List<EmployeeAvailabilityDTO>,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class EmployeeAvailabilityDTO(
    val username: String,
    val unavailable: List<UnavailableDayDTO>,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class UnavailableDayDTO(
    val date: String,
    val reason: UnavailabilityReason,
)

enum class UnavailabilityReason {
    LEAVE,
}
