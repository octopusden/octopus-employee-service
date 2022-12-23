package org.octopusden.employee.client.common.feign

import feign.Param
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class LocalDateExpander : Param.Expander {
    override fun expand(value: Any?): String {
        return (value as? LocalDate)
            ?.let { localDate ->
                isoLocalDateFormatter.format(localDate)
            } ?: ""
    }

    companion object {
        private val isoLocalDateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    }
}
