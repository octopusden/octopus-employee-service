package org.octopusden.employee.controller

import org.octopusden.employee.service.OneCService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for admin operations.
 */
@RestController
@RequestMapping("admin")
class AdminController(private val oneCService: OneCService) {
    /**
     * Check if the information about the required time is valid.
     */
    @GetMapping("one-c-integration-check")
    @PreAuthorize("permitAll()")
    fun isRequiredTimeValid(): Boolean = oneCService.isRequiredTimeValid()
}