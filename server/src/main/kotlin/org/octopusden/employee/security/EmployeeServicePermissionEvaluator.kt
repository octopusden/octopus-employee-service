package org.octopusden.employee.security

import org.octopusden.cloud.commons.security.BasePermissionEvaluator
import org.octopusden.cloud.commons.security.SecurityService
import org.springframework.stereotype.Component

@Component
class EmployeeServicePermissionEvaluator(securityService: SecurityService) : BasePermissionEvaluator(securityService)