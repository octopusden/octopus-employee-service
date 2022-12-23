package org.octopusden.employee.client.common.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class CustomerDTO(val instanceId: Long, val instanceName: String, val isEnabled: Boolean)
