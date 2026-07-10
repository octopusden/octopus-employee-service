package org.octopusden.employee.service

import org.octopusden.employee.client.common.exception.NotFoundException

interface AdService {
    /**
     * Returns the manager's sAMAccountName, or null if the user has no manager attribute.
     * Throws [NotFoundException] if [username] is not found in AD.
     */
    fun getManager(username: String): String?
}
