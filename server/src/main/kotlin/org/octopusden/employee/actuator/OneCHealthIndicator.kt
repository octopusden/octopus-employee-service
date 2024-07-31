package org.octopusden.employee.actuator

import org.octopusden.employee.service.onec.client.OneCClient
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.stereotype.Component

/**
 * The component to check integration wit oneC
 */
@Component
class OneCHealthIndicator(
    private val oneCClient: OneCClient
): HealthIndicator {

    override fun health(): Health {
        return try {
            oneCClient.getHealth()
            Health.up().build()
        } catch (e: Exception) {
            Health.down(e).build()
        }
    }
}