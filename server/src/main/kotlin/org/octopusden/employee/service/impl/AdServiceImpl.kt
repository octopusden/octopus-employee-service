package org.octopusden.employee.service.impl

import org.octopusden.employee.client.common.exception.NotFoundException
import org.octopusden.employee.config.AdProperties
import org.octopusden.employee.service.AdService
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.cache.annotation.Cacheable
import org.springframework.ldap.NameNotFoundException
import org.springframework.ldap.core.AttributesMapper
import org.springframework.ldap.core.LdapTemplate
import org.springframework.ldap.query.LdapQueryBuilder
import org.springframework.stereotype.Service
import javax.naming.InvalidNameException
import javax.naming.ldap.LdapName

@Service
@ConditionalOnProperty("ad.url")
class AdServiceImpl(
    private val ldapTemplate: LdapTemplate,
    private val adProperties: AdProperties,
) : AdService {
    @Cacheable(CACHE_MANAGERS)
    override fun getManager(username: String): String? {
        log.debug("getManager({})", username)

        val results = ldapTemplate.search(
            LdapQueryBuilder
                .query()
                .base(adProperties.baseDn)
                .attributes(ATTR_MANAGER)
                .where(ATTR_SAM_ACCOUNT_NAME)
                .`is`(username),
            AttributesMapper { attrs -> attrs.get(ATTR_MANAGER)?.get()?.toString() },
        )

        if (results.isEmpty()) throw NotFoundException("User '$username' not found in AD")

        val managerDn: String = results.first() ?: return null

        return try {
            ldapTemplate.lookup(
                LdapName(managerDn),
                arrayOf(ATTR_SAM_ACCOUNT_NAME),
                AttributesMapper { attrs -> attrs.get(ATTR_SAM_ACCOUNT_NAME)?.get()?.toString() },
            )
        } catch (e: NameNotFoundException) {
            log.warn("Manager DN '{}' for user '{}' not found in AD", managerDn, username)
            null
        } catch (e: InvalidNameException) {
            log.warn("Invalid manager DN '{}' for user '{}'", managerDn, username)
            null
        }
    }

    companion object {
        const val ATTR_SAM_ACCOUNT_NAME = "sAMAccountName"
        const val ATTR_MANAGER = "manager"
        const val CACHE_MANAGERS = "managers"
        private val log = LoggerFactory.getLogger(AdServiceImpl::class.java)
    }
}
