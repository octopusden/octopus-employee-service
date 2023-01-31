package org.octopusden.employee.service.onec.client

import org.octopusden.employee.client.common.feign.LocalDateExpander
import org.octopusden.employee.service.onec.client.dto.PlannedTimeDTO
import feign.Param
import feign.RequestLine
import java.time.LocalDate

interface OneCClient {
    @RequestLine("GET ru/hs/ow_http/getPlannedTime?user={employee}&dateFrom={dateFrom}&dateTo={dateTo}")
    fun getPlannedTime(
        @Param("employee") employee: String,
        @Param("dateFrom", expander = LocalDateExpander::class) fromDate: LocalDate,
        @Param("dateTo", expander = LocalDateExpander::class) toDate: LocalDate
    ): List<PlannedTimeDTO>

    @RequestLine("GET ru/hs/ow_http/ping")
    fun getHealth()
}
