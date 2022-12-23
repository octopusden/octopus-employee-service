package org.octopusden.employee.service.onec.client.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.math.BigDecimal
import java.util.Date

@JsonIgnoreProperties(ignoreUnknown = true)
data class PlannedTimeDTO(
    val date: Date,
    val user: String,
    val project: String,
    val activity: String,
    val md: BigDecimal
)
