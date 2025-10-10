import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.net.InetAddress
import java.util.zip.CRC32

plugins {
    java
    idea
    id("org.jetbrains.kotlin.jvm")
    id("io.github.gradle-nexus.publish-plugin")
    signing
}

val defaultVersion = "${
    with(CRC32()) {
        update(InetAddress.getLocalHost().hostName.toByteArray())
        value
    }
}-SNAPSHOT"

allprojects {
    group = "org.octopusden.octopus.employee"
    if (version == "unspecified") {
        version = defaultVersion
    }
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://ossrh-staging-api.central.sonatype.com/service/local/"))
            snapshotRepositoryUrl.set(uri("https://central.sonatype.com/repository/maven-snapshots/"))
            username.set(System.getenv("MAVEN_USERNAME"))
            password.set(System.getenv("MAVEN_PASSWORD"))
        }
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "idea")
    apply(plugin = "java")
    apply(plugin = "signing")

    java {
        withJavadocJar()
        withSourcesJar()
        JavaVersion.VERSION_21.let {
            sourceCompatibility = it
            targetCompatibility = it
        }
    }

    kotlin {
        compilerOptions.jvmTarget = JvmTarget.JVM_21
    }

    repositories {
        mavenCentral()
    }

    idea.module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    dependencies {
        constraints {
            implementation("org.jetbrains.kotlin:kotlin-stdlib:${project.properties["kotlin.version"]}")
        }
    }

    ext {
        System.getenv().let {
            set("signingRequired", it.containsKey("ORG_GRADLE_PROJECT_signingKey") && it.containsKey("ORG_GRADLE_PROJECT_signingPassword"))
            set("testPlatform", it.getOrDefault("TEST_PLATFORM", properties["test.platform"]))
            set("dockerRegistry", it.getOrDefault("DOCKER_REGISTRY", properties["docker.registry"]))
            set("octopusGithubDockerRegistry", it.getOrDefault("OCTOPUS_GITHUB_DOCKER_REGISTRY", project.properties["octopus.github.docker.registry"]))
            set("okdActiveDeadlineSeconds", it.getOrDefault("OKD_ACTIVE_DEADLINE_SECONDS", properties["okd.active-deadline-seconds"]))
            set("okdProject", it.getOrDefault("OKD_PROJECT", properties["okd.project"]))
            set("okdClusterDomain", it.getOrDefault("OKD_CLUSTER_DOMAIN", properties["okd.cluster-domain"]))
            set("okdWebConsoleUrl", (it.getOrDefault("OKD_WEB_CONSOLE_URL", properties["okd.web-console-url"]) as String).trimEnd('/'))
            set("authServerUrl", it.getOrDefault("AUTH_SERVER_URL", properties["auth-server.url"]))
            set("authServerRealm", it.getOrDefault("AUTH_SERVER_REALM", properties["auth-server.realm"]))
            set("authServerClientId", it.getOrDefault("AUTH_SERVER_CLIENT_ID", properties["auth-server.client-id"]))
            set("authServerClientSecret", it.getOrDefault("AUTH_SERVER_CLIENT_SECRET", properties["auth-server.client-secret"]))
            set("employeeServiceUser", it.getOrDefault("EMPLOYEE_SERVICE_USER", project.properties["employee-service.user"]))
            set("employeeServicePassword", it.getOrDefault("EMPLOYEE_SERVICE_PASSWORD", project.properties["employee-service.password"]))
        }
    }
    val supportedTestPlatforms = listOf("docker", "okd")
    if (project.ext["testPlatform"] !in supportedTestPlatforms) {
        throw IllegalArgumentException("Test platform must be set to one of the following $supportedTestPlatforms. Start gradle build with -Ptest.platform=... or set env variable TEST_PLATFORM")
    }
    val mandatoryProperties = mutableListOf("dockerRegistry", "octopusGithubDockerRegistry")
    if (project.ext["testPlatform"] == "okd") {
        mandatoryProperties.add("okdActiveDeadlineSeconds")
        mandatoryProperties.add("okdProject")
        mandatoryProperties.add("okdClusterDomain")
    }
    val undefinedProperties = mandatoryProperties.filter { (project.ext[it] as String).isBlank() }
    if (undefinedProperties.isNotEmpty()) {
        throw IllegalArgumentException(
            "Start gradle build with" +
                    (if (undefinedProperties.contains("dockerRegistry")) " -Pdocker.registry=..." else "") +
                    (if (undefinedProperties.contains("octopusGithubDockerRegistry")) " -Poctopus.github.docker.registry=..." else "") +
                    (if (undefinedProperties.contains("okdActiveDeadlineSeconds")) " -Pokd.active-deadline-seconds=..." else "") +
                    (if (undefinedProperties.contains("okdProject")) " -Pokd.project=..." else "") +
                    (if (undefinedProperties.contains("okdClusterDomain")) " -Pokd.cluster-domain=..." else "") +
                    " or set env variable(s):" +
                    (if (undefinedProperties.contains("dockerRegistry")) " DOCKER_REGISTRY" else "") +
                    (if (undefinedProperties.contains("octopusGithubDockerRegistry")) " OCTOPUS_GITHUB_DOCKER_REGISTRY" else "") +
                    (if (undefinedProperties.contains("okdActiveDeadlineSeconds")) " OKD_ACTIVE_DEADLINE_SECONDS" else "") +
                    (if (undefinedProperties.contains("okdProject")) " OKD_PROJECT" else "") +
                    (if (undefinedProperties.contains("okdClusterDomain")) " OKD_CLUSTER_DOMAIN" else "")
        )
    }
}

tasks.findByName("publish")?.dependsOn(":employee-service:dockerPushImage")