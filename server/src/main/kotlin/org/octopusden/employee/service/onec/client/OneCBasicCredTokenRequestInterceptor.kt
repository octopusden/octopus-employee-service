package org.octopusden.employee.service.onec.client

import feign.RequestInterceptor
import feign.RequestTemplate
import org.apache.http.HttpHeaders
import java.util.Base64

class OneCBasicCredTokenRequestInterceptor(private val username: String, private val password: String) :
    RequestInterceptor {
    override fun apply(template: RequestTemplate) {
        template.header(
            HttpHeaders.AUTHORIZATION,
            "Basic ${Base64.getEncoder().encodeToString("$username:$password".toByteArray())}"
        )
    }
}
