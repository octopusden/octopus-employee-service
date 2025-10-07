package org.octopusden.task

import com.google.common.io.Files
import com.google.common.net.HttpHeaders
import org.apache.http.entity.ContentType
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.mockserver.client.MockServerClient
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse
import java.io.File
import java.nio.charset.Charset

open class MigrateMockData : DefaultTask() {

    private val mockServerClient = MockServerClient("localhost", 1080)

    @get:Input
    lateinit var testDataDir: String

    @TaskAction
    fun startMockServer() {
        mockServerClient.reset()
        endpointToResponseFileName.forEach {
            generateMockserverData(it.key.first, it.key.second, testDataDir + File.separator + it.value, 200)
        }
        endpointNotFoundToResponseFileName.forEach {
            generateMockserverData(it.key.first, it.key.second, testDataDir + File.separator + it.value, 404)
        }
    }

    private fun generateMockserverData(endpoint: String, params: Map<String, String>, filename: String, status: Int) {
        val body = Files.asCharSource(File(filename), Charset.defaultCharset()).read()
        val request = HttpRequest.request()
            .withMethod("GET")
            .withPath(endpoint)
        params.forEach {
            request.withQueryStringParameter(it.key, it.value)
        }
        mockServerClient.`when`(request)
            .respond {
                logger.debug(
                    "MockServer request: ${it.method} ${it.path} ${it.queryStringParameterList.joinToString(",")} ${
                        it.pathParameterList.joinToString(
                            ","
                        )
                    }"
                )
                HttpResponse.response()
                    .withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.mimeType)
                    .withBody(body)
                    .withStatusCode(status)
            }
    }

    companion object {
        private const val jqlTemplate = "Employee in (%s) AND project in (\"Calendar RCIS\", \"Calendar\") AND \"Leave from date\" <= startOfDay() AND \"Leave to date\" >= endOfDay() AND status not in (Canceled, Rejected)"
        private val endpointToResponseFileName = mapOf(
            "/one-c/ru/hs/ow_http/ping" to emptyMap<String, String>() to "one-c-health.json",
            "/one-c/ru/hs/ow_http/getPlannedTime" to mapOf(
                "dateFrom" to "2021-01-01",
                "dateTo" to "2021-01-31",
                "user" to "employee"
            ) to "one-c-get-planned-time-jan-01-31.json",
            "/one-c/ru/hs/ow_http/getPlannedTime" to mapOf(
                "dateFrom" to "2021-01-01",
                "dateTo" to "2021-01-10",
                "user" to "employee"
            ) to "one-c-get-planned-time-jan-01-10.json",
            "/one-c/ru/hs/ow_http/getPlannedTime" to mapOf(
                "dateFrom" to "2021-01-11",
                "dateTo" to "2021-01-31",
                "user" to "employee"
            ) to "one-c-get-planned-time-jan-11-31.json",
            "/one-c/ru/hs/ow_http/getWorkingDays" to mapOf(
                "dateFrom" to "2021-01-01",
                "dateTo" to "2021-01-10"
            ) to "one-c-get-working-days-01-10.json",
            "/one-c/ru/hs/ow_http/getWorkingDays" to mapOf(
                "dateFrom" to "2021-01-01",
                "dateTo" to "2021-01-31"
            ) to "one-c-get-working-days-01-31.json",
            "/one-c/ru/hs/ow_http/getWorkingDays" to mapOf(
                "dateFrom" to "2021-01-11",
                "dateTo" to "2021-01-31"
            ) to "one-c-get-working-days-01-31.json",
            "/jira1/rest/api/2/user" to mapOf(
                "username" to "employee",
            ) to "jira1/jira-employee.json",
            "/jira1/rest/api/2/user" to mapOf(
                "username" to "inactive",
            ) to "jira1/jira-inactive.json",
            "/jira2/rest/api/2/search" to mapOf(
                "jql" to jqlTemplate.format("absent1,absent2,employee")
            ) to "jira2/calendar-absent1-2-response.json",
            "/jira2/rest/api/2/search" to mapOf(
                "jql" to jqlTemplate.format("absent1,absent2")
            ) to "jira2/calendar-absent1-2-response.json",
            "/jira2/rest/api/2/search" to mapOf(
                "jql" to jqlTemplate.format("employee")
            ) to "jira2/calendar-employee-response.json",
            "/jira2/rest/api/2/search" to mapOf(
                "jql" to jqlTemplate.format("unavailable")
            ) to "jira2/calendar-unavailable-response.json",
            "/jira2/rest/scriptrunner/latest/custom/localInstancesDataForRnd" to mapOf<String, String>(
            ) to "jira2/customers.json"
        )
        private val endpointNotFoundToResponseFileName = mapOf(
            "/jira1/rest/api/2/user" to mapOf(
                "username" to "nonexistent",
            ) to "jira1/jira-nonexistent.json"
        )
    }
}
