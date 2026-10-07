package org.octopusden.employee.service

import org.apache.commons.lang.text.StrSubstitutor
import java.util.stream.Collectors

fun formatJQL(
    jql: String,
    users: Collection<String>,
): String =
    StrSubstitutor(
        mapOf(
            "usernames" to users
                .stream()
                .collect(Collectors.joining(",")),
        ),
        "{",
        "}",
    ).replace(jql)

/**
 * Quotes [value] as a JQL string literal, escaping backslashes and double quotes, so values with
 * reserved characters (e.g. `alice@example.com`) produce valid JQL.
 */
fun toJqlString(value: String): String = "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\""
