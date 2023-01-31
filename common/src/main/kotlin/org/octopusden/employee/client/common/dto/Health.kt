package org.octopusden.employee.client.common.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * The model to transfer a health information
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class Health(val status: String)