package org.octopusden.employee.client.common.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * [reason] is a plain string so that clients keep working when the server adds new reasons.
 * Known values: [REASON_LEAVE].
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class UnavailableDayDTO(
    val date: String,
    val reason: String,
) {
    companion object {
        const val REASON_LEAVE = "LEAVE"
    }
}
