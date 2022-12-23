package org.octopusden.employee

import java.time.LocalDate
import java.time.format.DateTimeFormatter

abstract class BaseTest {

    protected fun String.toLocalDate(): LocalDate = LocalDate.parse(this, isoLocalDateFormatter)

    companion object {
        @JvmStatic
        val isoLocalDateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    }
}