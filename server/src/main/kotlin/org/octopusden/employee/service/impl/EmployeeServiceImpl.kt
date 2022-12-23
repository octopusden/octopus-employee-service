package org.octopusden.employee.service.impl

import org.octopusden.employee.client.common.dto.Employee
import org.octopusden.employee.client.common.dto.RequiredTimeDTO
import org.octopusden.employee.client.common.exception.NotFoundException
import org.octopusden.employee.config.EmployeeServiceProperties
import org.octopusden.employee.service.EmployeeService
import org.octopusden.employee.service.OneCService
import org.octopusden.employee.service.formatJQL
import org.octopusden.employee.service.jira.client.common.JiraClientException
import org.octopusden.employee.service.jira.client.common.JiraUser
import org.octopusden.employee.service.jira.client.jira1.Jira1Client
import org.octopusden.employee.service.jira.client.jira2.Jira2Client
import org.apache.http.HttpStatus
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class EmployeeServiceImpl(
    private val oneCService: OneCService,
    private val jira1Client: Jira1Client,
    private val jira2Client: Jira2Client,
    private val employeeServiceProperties: EmployeeServiceProperties
) :
    EmployeeService {

    override fun getEmployee(username: String): Employee {
        val user = try {
            jira1Client.getUser(username)
        } catch (e: JiraClientException) {
            when (e.status) {
                HttpStatus.SC_NOT_FOUND -> throw NotFoundException(e.message!!)
                else -> throw e
            }
        }
        return Employee(username, user.active)
    }

    override fun getRequiredTime(username: String, fromDate: LocalDate, toDate: LocalDate): RequiredTimeDTO {
        return oneCService.getRequiredTime(getEmployee(username), fromDate, toDate)
    }

    override fun isUserAvailable(username: String): Boolean {
        return jira2Client
            .getAbsentUserNowIssues(formatJQL(employeeServiceProperties.userAvailability.jql, setOf(username)))
            .issues
            .isEmpty()
            .also { available ->
                if (available) {
                    checkUserExists(username)
                }
                log.debug("isUserAvailable($username)=$available")
            }
    }

    private fun checkUserExists(username: String) {
        getEmployee(username)
    }

    override fun getEmployeeAvailableEarlier(employees: Set<String>): Employee {
        val employeeAbsents = jira2Client.getAbsentUserNowIssues(formatJQL(employeeServiceProperties.userAvailability.jql, employees))
            .issues
            .map { issue -> issue.fields }
            .groupBy(
                { fields -> fields.employee.name },
                { fields -> UserAbsence(fields.employee, fields.from, fields.to) })

        val availableEmployees = employees.filter { employee -> !employeeAbsents.containsKey(employee) }
            .map { employee -> getEmployee(employee) }
            .filter { employee -> employee.active }

        return availableEmployees.firstOrNull()
            ?: employeeAbsents.values
                .flatten()
                .minByOrNull { it.end }
                ?.let { Employee(it.employee.name, it.employee.active) } ?: throw IllegalStateException()

    }

    data class UserAbsence(val employee: JiraUser, val start: LocalDate, val end: LocalDate)

    companion object {
        private val log: Logger = LoggerFactory.getLogger(EmployeeServiceImpl::class.java)
    }
}
