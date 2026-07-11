package org.octopusden.employee.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("ad")
data class AdProperties(
    val url: String = "",
    val userDn: String = "",
    val password: String = "",
    val baseDn: String = "",
) {
    override fun toString(): String = "AdProperties(url='$url', userDn='$userDn', password='***', baseDn='$baseDn')"
}
