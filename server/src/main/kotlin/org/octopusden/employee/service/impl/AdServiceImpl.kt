package org.octopusden.employee.service.impl

import org.octopusden.employee.client.common.exception.NotFoundException
import org.octopusden.employee.config.AdProperties
import org.octopusden.employee.service.AdService
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.cache.annotation.Cacheable
import org.springframework.ldap.core.AttributesMapper
import org.springframework.ldap.core.LdapTemplate
import org.springframework.ldap.query.LdapQueryBuilder
import org.springframework.stereotype.Service
import javax.naming.ldap.LdapName

@Service
@ConditionalOnProperty("ad.url")
class AdServiceImpl(
    private val ldapTemplate: LdapTemplate,
    private val adProperties: AdProperties,
) : AdService {

    @Cacheable("managers")
    override fun getManager(username: String): String? {
        log.debug("getManager({})", username)

        val results = ldapTemplate.search(
            LdapQueryBuilder.query()
                .base(adProperties.baseDn)
                .where("sAMAccountName").`is`(username),
            AttributesMapper { attrs -> attrs.get("manager")?.get()?.toString() },
        )

        if (results.isEmpty()) throw NotFoundException("User '$username' not found in AD")

        val managerDn: String = results.first() ?: return null

        return ldapTemplate.lookup(
            LdapName(managerDn),
            arrayOf("sAMAccountName"),
            AttributesMapper { attrs -> attrs.get("sAMAccountName")?.get()?.toString() },
        )
    }

    companion object {
        private val log = LoggerFactory.getLogger(AdServiceImpl::class.java)
    }
}
