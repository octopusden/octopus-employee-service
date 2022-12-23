package org.octopusden.employee.service.jira.client.common

import feign.RequestInterceptor
import feign.RequestTemplate
import org.apache.http.HttpHeaders
import java.util.Base64

class JiraBasicCredTokenRequestInterceptor(private val username: String, private val password: String) :
    RequestInterceptor {
    override fun apply(template: RequestTemplate) {
        template.header(
            HttpHeaders.AUTHORIZATION,
            "Basic ${Base64.getEncoder().encodeToString("$username:$password".toByteArray())}"
        )
        template.header("X-Atlassian-Token", "no-check")
    }
}
