package org.octopusden.employee.service

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class UserAvailableUtilsTest {

    @Test
    fun shouldReturnJQL() {
        Assertions.assertEquals( "Employee in (name1,name2)", formatJQL("Employee in ({usernames})", listOf("name1", "name2")))
    }

}