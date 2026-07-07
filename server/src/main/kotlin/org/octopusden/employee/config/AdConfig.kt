package org.octopusden.employee.config

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.ldap.core.LdapTemplate
import org.springframework.ldap.core.support.LdapContextSource
import java.util.concurrent.TimeUnit

@Configuration
@EnableCaching
@EnableConfigurationProperties(AdProperties::class)
@ConditionalOnProperty("ad.url")
class AdConfig(private val adProperties: AdProperties) {

    @Bean
    fun ldapContextSource(): LdapContextSource = LdapContextSource().apply {
        setUrl(adProperties.url)
        setUserDn(adProperties.userDn)
        setPassword(adProperties.password)
        afterPropertiesSet()
    }

    @Bean
    fun ldapTemplate(contextSource: LdapContextSource): LdapTemplate =
        LdapTemplate(contextSource).apply {
            setIgnorePartialResultException(true)
        }

    @Bean
    fun cacheManager(): CacheManager = CaffeineCacheManager("managers").apply {
        setCaffeine(Caffeine.newBuilder().maximumSize(1000).expireAfterWrite(5, TimeUnit.MINUTES))
    }
}
