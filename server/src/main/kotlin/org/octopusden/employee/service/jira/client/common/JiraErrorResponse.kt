package org.octopusden.employee.service.jira.client.common

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class JiraErrorResponse(
    val errorMessages: List<String>
)
