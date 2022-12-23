package org.octopusden.employee.service

import org.octopusden.employee.client.common.dto.Employee
import org.octopusden.employee.client.common.dto.RequiredTimeDTO
import java.time.LocalDate

interface OneCService {
    fun getRequiredTime(employee: Employee, fromDate: LocalDate, toDate: LocalDate): RequiredTimeDTO
}
