package org.octopusden.employee.service.impl

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.octopusden.employee.client.common.exception.NotFoundException
import org.octopusden.employee.config.AdProperties
import org.springframework.ldap.NameNotFoundException
import org.springframework.ldap.core.AttributesMapper
import org.springframework.ldap.core.LdapTemplate
import org.springframework.ldap.query.LdapQuery
import javax.naming.Name

class AdServiceImplTest {
    private val ldapTemplate = Mockito.mock(LdapTemplate::class.java)
    private val adProperties = AdProperties(baseDn = "OU=Users,DC=example,DC=com")
    private val adService = AdServiceImpl(ldapTemplate, adProperties)

    @Test
    fun getManagerReturnsManagerUsername() {
        stubSearch(listOf("CN=Manager,OU=Users,DC=example,DC=com"))
        stubLookup("managerSam")

        Assertions.assertEquals("managerSam", adService.getManager("employee"))
    }

    @Test
    fun getManagerReturnsNullWhenUserHasNoManagerAttribute() {
        stubSearch(listOf(null))

        Assertions.assertNull(adService.getManager("employee"))
        Mockito
            .verify(ldapTemplate, Mockito.never())
            .lookup(any(Name::class.java), any<Array<String>>(), any<AttributesMapper<String?>>())
    }

    @Test
    fun getManagerThrowsNotFoundWhenUserMissing() {
        stubSearch(emptyList())

        Assertions.assertThrows(NotFoundException::class.java) { adService.getManager("nobody") }
    }

    @Test
    fun getManagerReturnsNullWhenManagerDnIsStale() {
        stubSearch(listOf("CN=Ghost,OU=Users,DC=example,DC=com"))
        Mockito
            .`when`(ldapTemplate.lookup(any(Name::class.java), any<Array<String>>(), any<AttributesMapper<String?>>()))
            .thenThrow(NameNotFoundException("not found"))

        Assertions.assertNull(adService.getManager("employee"))
    }

    @Test
    fun getManagerReturnsNullWhenManagerDnIsMalformed() {
        stubSearch(listOf("not-a-valid-dn-no-equals"))

        Assertions.assertNull(adService.getManager("employee"))
        Mockito
            .verify(ldapTemplate, Mockito.never())
            .lookup(any(Name::class.java), any<Array<String>>(), any<AttributesMapper<String?>>())
    }

    private fun stubSearch(result: List<String?>) {
        Mockito
            .`when`(ldapTemplate.search(any<LdapQuery>(), any<AttributesMapper<String?>>()))
            .thenReturn(result)
    }

    private fun stubLookup(result: String?) {
        Mockito
            .`when`(ldapTemplate.lookup(any(Name::class.java), any<Array<String>>(), any<AttributesMapper<String?>>()))
            .thenReturn(result)
    }
}
