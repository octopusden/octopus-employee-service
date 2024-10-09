package org.octopusden.employee.actuator

import org.octopusden.employee.config.OneCProperties
import org.octopusden.employee.service.onec.client.OneCClient
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.stereotype.Component

/**
 * The component to check integration wit oneC report
 */
@Component
class OneCReportHealthIndicator(
    private val oneCProperties: OneCProperties,
    private val oneCClient: OneCClient
) : HealthIndicator {

    override fun health(): Health {
        val health = oneCProperties.health ?: return Health.unknown().build()
        return try {
            val result = oneCClient.getPlannedTime(health.user, health.startDate, health.endDate)
            if (result.isEmpty()) {
                Health.down()
                    .withDetail(
                        "error",
                        "No data for user ${health.user} in period ${health.startDate} - ${health.endDate}"
                    )
                    .build()
            } else {
                Health.up().build()
            }
        } catch (e: Exception) {
            Health.down(e).build()
        }
    }
}