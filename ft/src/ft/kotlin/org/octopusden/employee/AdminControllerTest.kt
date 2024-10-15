package org.octopusden.employee

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AdminControllerTest {

    @Test
    fun shouldCheckOneCIntegration() {
        val status = client.oneCInntegrationCheck()
        Assertions.assertTrue(status)
    }

    companion object {
        private val client = TestFtUtils.getUnsecuredClient()
    }
}