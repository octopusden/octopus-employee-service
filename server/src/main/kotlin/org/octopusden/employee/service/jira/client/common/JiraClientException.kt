package org.octopusden.employee.service.jira.client.common

class JiraClientException(val status: Int, message: String) : RuntimeException(message)
