package org.octopusden.employee.client.common.dto

data class ErrorResponse(val errorCode: EmployeeServiceErrorCode, val errorMessage: String)
