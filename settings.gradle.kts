pluginManagement {
    plugins {
        val kotlinVersion = extra["kotlin.version"] as String
        id("org.octopusden.release-management") version(extra["release-management.version"] as String)
        id("org.springframework.boot") version (extra["spring-boot.version"] as String)
        id("org.jetbrains.kotlin.jvm") version kotlinVersion
        id("org.jetbrains.kotlin.plugin.spring") version kotlinVersion
        id("org.jetbrains.kotlin.plugin.jpa") version kotlinVersion
        id("org.jetbrains.kotlin.plugin.allopen") version kotlinVersion
        id("org.jetbrains.kotlin.plugin.noarg") version kotlinVersion
        id("io.github.gradle-nexus.publish-plugin") version("1.1.0") apply(false)
    }
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "octopus-employee-service"

include(":server")
findProject(":server")?.name = "employee-service"

include(":common")
include(":client")
include(":ft")
include(":test-common")
