pluginManagement {
    plugins {
        val kotlinPluginVersion = extra["kotlin-plugin.version"] as String
        val springBootPluginVersion = extra["spring-boot-plugin.version"] as String

        id("org.jetbrains.kotlin.jvm") version kotlinPluginVersion
        id("org.jetbrains.kotlin.plugin.spring") version kotlinPluginVersion
        id("org.jetbrains.kotlin.plugin.jpa") version kotlinPluginVersion
        id("org.jetbrains.kotlin.plugin.allopen") version kotlinPluginVersion
        id("org.jetbrains.kotlin.plugin.noarg") version kotlinPluginVersion
        id("org.springframework.boot") version springBootPluginVersion
        id("io.github.gradle-nexus.publish-plugin") version ("2.0.0") apply (false)
        id("org.octopusden.octopus.oc-template") version (extra["octopus-oc-template.version"] as String)
        id("com.avast.gradle.docker-compose") version (extra["docker-compose-plugin.version"] as String)
        // Octopus quality-gates convention plugin + Kotlin static-analysis tools.
        id("io.gitlab.arturbosch.detekt") version (extra["detekt.version"] as String)
        id("org.jlleitschuh.gradle.ktlint") version (extra["ktlint.version"] as String)
        id("org.octopusden.octopus-quality") version (extra["octopus-quality.version"] as String)
        id("org.sonarqube") version (extra["sonarqube.version"] as String)
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
