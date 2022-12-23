package org.octopusden.employee.service

import org.apache.commons.lang.text.StrSubstitutor
import java.util.stream.Collectors

fun formatJQL(jql: String, users: Collection<String>): String = StrSubstitutor(
    mapOf(
        "usernames" to users
            .stream()
            .collect(Collectors.joining(","))
    ),
    "{", "}"
)
    .replace(jql)