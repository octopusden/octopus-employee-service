package org.octopusden.employee.service.jira.client.common

data class JiraIssuesDTO<T: BaseIssueFieldsDTO>(val issues: Collection<GetJiraIssueDTO<T>>)
