package org.octopusden.employee

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ActuatorControllerTest {

    private val buildVersion: String = System.getProperty("buildVersion")

    @Test
    fun getServerInfoTest() {
        val serverInfo = client.getServerInfo()
        Assertions.assertEquals(buildVersion, serverInfo.build.version)
    }

    @Test
    fun testOneCHealth() {
        val health = client.oneCHealth()
        Assertions.assertEquals("UP", health.status)
    }

    companion object {
        private val client = TestFtUtils.getUnsecuredClient()
    }
}
