package org.octopusden.employee.service.jira.client.common

import com.fasterxml.jackson.databind.ObjectMapper
import feign.Response
import feign.codec.ErrorDecoder
import org.slf4j.LoggerFactory

class JiraErrorDecoder(private val objectMapper: ObjectMapper) : ErrorDecoder {
    override fun decode(methodKey: String, response: Response): Exception {
        val message = try {
            objectMapper.readValue(response.body().asInputStream(), JiraErrorResponse::class.java)
                .errorMessages.joinToString(separator = ". ")
        } catch (e: Exception) {
            log.error(e.message, e)
            "Error executing Jira request: $methodKey"
        }
        return JiraClientException(response.status(), message)
    }

    data class JiraErrorResponse(
        val errorMessages: List<String>,
    )

    companion object {
        private val log = LoggerFactory.getLogger(JiraErrorDecoder::class.java)
    }
}
