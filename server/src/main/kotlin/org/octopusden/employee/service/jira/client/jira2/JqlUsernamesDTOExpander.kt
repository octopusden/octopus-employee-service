package org.octopusden.employee.service.jira.client.jira2

import feign.Param

class JqlUsernamesDTOExpander : Param.Expander {
    override fun expand(value: Any?): String {
        return (value as? JqlUsernamesDTO)
            ?.usernames
            ?.joinToString(",")
            ?: ""
    }
}
