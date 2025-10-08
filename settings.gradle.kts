pluginManagement {
    plugins {
        val kotlinVersion = extra["kotlin.version"] as String
        val springBootVersion = extra["spring-boot.version"] as String

        id("org.jetbrains.kotlin.jvm") version kotlinVersion
        id("org.jetbrains.kotlin.plugin.spring") version kotlinVersion
        id("org.jetbrains.kotlin.plugin.jpa") version kotlinVersion
        id("org.jetbrains.kotlin.plugin.allopen") version kotlinVersion
        id("org.jetbrains.kotlin.plugin.noarg") version kotlinVersion
        id("org.springframework.boot") version springBootVersion
        id("io.github.gradle-nexus.publish-plugin") version ("1.1.0") apply (false)
        id("org.gradle.toolchains.foojay-resolver-convention") version("0.7.0")
        id("org.octopusden.octopus.oc-template") version (extra["octopus-oc-template.version"] as String)
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
