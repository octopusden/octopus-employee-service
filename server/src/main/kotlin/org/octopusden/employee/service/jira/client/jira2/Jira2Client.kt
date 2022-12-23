package org.octopusden.employee.service.jira.client.jira2

import org.octopusden.employee.client.common.dto.CustomerDTO
import org.octopusden.employee.service.jira.client.common.BaseJiraClient
import org.octopusden.employee.service.jira.client.common.JiraIssuesDTO
import org.octopusden.employee.service.jira.client.jira2.AbsenceIssueFieldsDTO.Companion.employeeFieldId
import org.octopusden.employee.service.jira.client.jira2.AbsenceIssueFieldsDTO.Companion.fromDateFieldId
import org.octopusden.employee.service.jira.client.jira2.AbsenceIssueFieldsDTO.Companion.toDateFieldId
import feign.Param
import feign.RequestLine

private const val LIMIT = 100

interface Jira2Client : BaseJiraClient {
    @RequestLine(
        "GET /rest/api/2/search" +
                "?jql={jql}" +
                "&maxResults=$LIMIT" +
                "&startAt=0" +
                "&fields=$employeeFieldId,$fromDateFieldId,$toDateFieldId"
    )
    fun getAbsentUserNowIssues(@Param("jql") jql: String): JiraIssuesDTO<AbsenceIssueFieldsDTO>

    @RequestLine("GET rest/scriptrunner/latest/custom/localInstancesDataForRnd")
    fun getCustomers(): Set<CustomerDTO>
}
