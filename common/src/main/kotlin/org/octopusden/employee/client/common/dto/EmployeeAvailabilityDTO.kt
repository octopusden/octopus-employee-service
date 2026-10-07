package org.octopusden.employee.client.common.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class EmployeeAvailabilityDTO(
    val username: String,
    val unavailable: List<UnavailableDayDTO>,
)
