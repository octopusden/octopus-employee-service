package org.octopusden.employee.service.impl

import org.apache.http.HttpStatus
import org.octopusden.employee.client.common.dto.Employee
import org.octopusden.employee.client.common.dto.EmployeeAvailabilityDTO
import org.octopusden.employee.client.common.dto.EmployeesAvailabilityDTO
import org.octopusden.employee.client.common.dto.ManagerDTO
import org.octopusden.employee.client.common.dto.RequiredTimeDTO
import org.octopusden.employee.client.common.dto.UnavailableDayDTO
import org.octopusden.employee.client.common.dto.WorkingDaysDTO
import org.octopusden.employee.client.common.exception.BadRequestException
import org.octopusden.employee.client.common.exception.NotFoundException
import org.octopusden.employee.config.EmployeeServiceProperties
import org.octopusden.employee.config.EmployeeServiceProperties.UserAvailability
import org.octopusden.employee.service.AdService
import org.octopusden.employee.service.EmployeeService
import org.octopusden.employee.service.OneCService
import org.octopusden.employee.service.formatJQL
import org.octopusden.employee.service.jira.client.common.JiraClientException
import org.octopusden.employee.service.jira.client.common.JiraUser
import org.octopusden.employee.service.jira.client.jira1.Jira1Client
import org.octopusden.employee.service.jira.client.jira2.AbsenceIssueFieldsDTO
import org.octopusden.employee.service.jira.client.jira2.Jira2Client
import org.octopusden.employee.service.toJqlString
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.ObjectProvider
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class EmployeeServiceImpl(
    private val oneCService: OneCService,
    private val jira1Client: Jira1Client,
    private val jira2Client: Jira2Client,
    private val employeeServiceProperties: EmployeeServiceProperties,
    private val adService: ObjectProvider<AdService>,
) : EmployeeService {
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

    override fun getRequiredTime(
        username: String,
        fromDate: LocalDate,
        toDate: LocalDate,
    ): RequiredTimeDTO = oneCService.getRequiredTime(getEmployee(username), fromDate, toDate)

    override fun isUserAvailable(username: String): Boolean =
        jira2Client
            .getAbsentUserNowIssues(formatJQL(employeeServiceProperties.userAvailability.jql, setOf(username)))
            .issues
            .isEmpty()
            .also { available ->
                if (available) {
                    checkUserExists(username)
                }
                log.debug("isUserAvailable($username)=$available")
            }

    private fun checkUserExists(username: String) {
        getEmployee(username)
    }

    override fun getEmployeeAvailableEarlier(employees: Set<String>): Employee {
        val employeeAbsents = jira2Client
            .getAbsentUserNowIssues(formatJQL(employeeServiceProperties.userAvailability.jql, employees))
            .issues
            .map { issue -> issue.fields }
            .groupBy(
                { fields -> fields.employee.name },
                { fields -> UserAbsence(fields.employee, fields.from, fields.to) },
            )

        val availableEmployees = employees
            .filter { employee -> !employeeAbsents.containsKey(employee) }
            .map { employee -> getEmployee(employee) }
            .filter { employee -> employee.active }

        return availableEmployees.firstOrNull()
            ?: employeeAbsents.values
                .flatten()
                .minByOrNull { it.end }
                ?.let { Employee(it.employee.name, it.employee.active) } ?: throw IllegalStateException()
    }

    override fun getWorkingDays(
        fromDate: LocalDate,
        toDate: LocalDate,
    ): WorkingDaysDTO =
        oneCService
            .getWorkingDays(fromDate, toDate)
            .also { workingDaysDTO -> log.debug("getWorkingDays($fromDate,$toDate)=$workingDaysDTO") }

    override fun getManager(username: String): ManagerDTO {
        log.debug("getManager({})", username)
        val svc = adService.getIfAvailable() ?: run {
            checkUserExists(username)
            return ManagerDTO(null)
        }
        return ManagerDTO(svc.getManager(username))
    }

    override fun getAvailability(
        employees: Set<String>,
        fromDate: LocalDate?,
        toDate: LocalDate?,
    ): EmployeesAvailabilityDTO {
        val from = fromDate ?: LocalDate.now()
        val to = toDate ?: from.plusMonths(1)
        if (from.isAfter(to)) {
            throw BadRequestException("fromDate '$from' must not be after toDate '$to'")
        }
        employees.forEach { employee -> checkUserExists(employee) }
        // Reuse the configured "absent today" query: a leave overlaps the period if it starts by `to` and ends from `from`
        val jql = formatJQL(employeeServiceProperties.userAvailability.jql, employees.map { employee -> toJqlString(employee) })
            .replace(UserAvailability.START_OF_DAY, "\"$to\"")
            .replace(UserAvailability.END_OF_DAY, "\"$from\"")

        // Expand each leave into calendar dates, clipped to the requested period
        val leaveDays = getAllAbsences(jql)
            .filter { fields -> !fields.from.isAfter(to) && !fields.to.isBefore(from) }
            .groupBy({ fields -> fields.employee.name }) { fields ->
                fields.from
                    .coerceAtLeast(from)
                    .datesUntil(fields.to.coerceAtMost(to).plusDays(1))
                    .toList()
            }.mapValues { (_, dates) -> dates.flatten().toSortedSet() }

        return EmployeesAvailabilityDTO(
            from.toString(),
            to.toString(),
            employees.map { employee ->
                EmployeeAvailabilityDTO(
                    employee,
                    leaveDays[employee].orEmpty().map { date -> UnavailableDayDTO(date.toString(), UnavailableDayDTO.REASON_LEAVE) },
                )
            },
        )
    }

    private fun getAllAbsences(jql: String): List<AbsenceIssueFieldsDTO> {
        val absences = mutableListOf<AbsenceIssueFieldsDTO>()
        do {
            val page = jira2Client.getAbsentUserNowIssues(jql, absences.size)
            absences += page.issues.map { issue -> issue.fields }
        } while (page.issues.isNotEmpty() && absences.size < page.total)
        return absences
    }

    data class UserAbsence(
        val employee: JiraUser,
        val start: LocalDate,
        val end: LocalDate,
    )

    companion object {
        private val log: Logger = LoggerFactory.getLogger(EmployeeServiceImpl::class.java)
    }
}
