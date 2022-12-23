package org.octopusden.employee.controller

import org.octopusden.employee.client.common.dto.EmployeeServiceErrorCode
import org.octopusden.employee.client.common.dto.ErrorResponse
import org.octopusden.employee.client.common.exception.EmployeeServiceException
import org.octopusden.employee.client.common.exception.NotFoundException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus

@ControllerAdvice
class ExceptionInfoHandler {

    @ExceptionHandler(NotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    fun handleNotFound(exception: EmployeeServiceException) = getErrorResponse(exception)

    @ExceptionHandler(AccessDeniedException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ResponseBody
    fun handleAccessDenied(exception: AccessDeniedException) = getErrorResponse(exception)

    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    @Order(100)
    fun handleException(exception: Exception): ErrorResponse {
        log.error(exception.message ?: "Internal error", exception)
        return getErrorResponse(exception)
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(ExceptionInfoHandler::class.java)

        private fun getErrorResponse(exception: Exception): ErrorResponse {
            val errorCode = EmployeeServiceErrorCode.getErrorCode(exception)
            return ErrorResponse(
                errorCode, exception.message
                    ?: errorCode.simpleMessage
            )
        }
    }
}
