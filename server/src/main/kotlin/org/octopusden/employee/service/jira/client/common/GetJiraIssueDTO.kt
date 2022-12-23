package org.octopusden.employee.service.jira.client.common

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
data class GetJiraIssueDTO<T: BaseIssueFieldsDTO> (
    @JsonProperty("id") val id: String,
    @JsonProperty("key") val key: String,
    @JsonProperty("fields") val fields: T,
)
