package org.octopusden.employee.client.common.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class ManagerDTO(val manager: String?)
