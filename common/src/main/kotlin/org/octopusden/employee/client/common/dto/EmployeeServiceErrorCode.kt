package org.octopusden.employee.client.common.dto

import org.octopusden.employee.client.common.exception.NotFoundException


enum class EmployeeServiceErrorCode(private val function: (message: String) -> Exception, val simpleMessage: String) {
    OTHER({ m -> IllegalStateException(m) }, "Internal server error"),
    NOT_FOUND({ m -> NotFoundException(m) }, "Not Found");

    fun getException(message: String): Exception {
        return function.invoke(message)
    }

    companion object {
        fun getErrorCode(exception: Exception): EmployeeServiceErrorCode {
            val qualifiedName = exception::class.qualifiedName
            return values().find { v -> v.function.invoke("")::class.qualifiedName == qualifiedName }
                ?: OTHER
        }
    }
}
