package org.octopusden.employee.client.impl

interface EmployeeServiceClientParametersProvider {
    fun getBearerToken(): String?
    fun getApiUrl(): String
    fun getTimeRetryInMillis(): Int
    fun getBasicCredentials(): String?
    fun getConnectTimeoutMillis(): Int = 300_000
    fun getReadTimeoutMillis(): Int = 300_000
}
