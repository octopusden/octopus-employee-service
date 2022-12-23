package org.octopusden.employee.client.common.exception

abstract class EmployeeServiceException(message: String): RuntimeException(message)

class NotFoundException(message: String) : EmployeeServiceException(message)
