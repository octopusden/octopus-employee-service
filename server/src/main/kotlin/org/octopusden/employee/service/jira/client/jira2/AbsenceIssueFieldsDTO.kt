package org.octopusden.employee.service.jira.client.jira2

import com.fasterxml.jackson.annotation.JsonProperty
import org.octopusden.employee.service.jira.client.common.BaseIssueFieldsDTO
import org.octopusden.employee.service.jira.client.common.JiraUser
import java.time.LocalDate

data class AbsenceIssueFieldsDTO(
    @JsonProperty(employeeFieldId) val employee: JiraUser,
    @JsonProperty(fromDateFieldId) val from: LocalDate,
    @JsonProperty(toDateFieldId) val to: LocalDate
): BaseIssueFieldsDTO() {
    companion object {
        const val employeeFieldId = "customfield_16402"
        const val fromDateFieldId = "customfield_10800"
        const val toDateFieldId = "customfield_10801"
    }
}