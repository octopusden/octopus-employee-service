package org.octopusden.employee.service.jira.client.common

import feign.Param
import feign.RequestLine

interface BaseJiraClient {
    @RequestLine("GET /rest/api/2/user?username={username}")
    fun getUser(@Param("username") username: String): JiraUser
}