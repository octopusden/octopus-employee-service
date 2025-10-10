import org.gradle.kotlin.dsl.provideDelegate
import org.octopusden.task.MigrateMockData

plugins {
    id("org.octopusden.octopus.oc-template")
    id("com.avast.gradle.docker-compose") version "0.16.9"
}

fun String.getPort() = when (this) {
    "gateway" -> 8765
    "employee" -> 8080
    "mockserver" -> 1080
    else -> throw Exception("Unknown service '$this'")
}
fun getOkdExternalHost(serviceName: String) = "${ocTemplate.getPod(serviceName)}-service:${serviceName.getPort()}"
fun String.getExt() = project.ext[this] as String

dockerCompose {
    useComposeFiles.add("${projectDir}${File.separator}docker${File.separator}docker-compose.yml")
    waitForTcpPorts = true
    captureContainersOutputToFiles = layout.buildDirectory.file("docker-logs").get().asFile
    environment.putAll(
        mapOf(
            "EMPLOYEE_SERVICE_VERSION" to project.version,
            "API_GATEWAY_VERSION" to project.properties["api-gateway.version"] as String,
            "MOCK_SERVER_VERSION" to project.properties["mockserver.version"] as String,
            "DOCKER_REGISTRY" to "dockerRegistry".getExt(),
            "OCTOPUS_GITHUB_DOCKER_REGISTRY" to "octopusGithubDockerRegistry".getExt(),
            "AUTH_SERVER_URL" to "authServerUrl".getExt(),
            "AUTH_SERVER_REALM" to "authServerRealm".getExt(),
            "AUTH_SERVER_CLIENT_ID" to "authServerClientId".getExt(),
            "AUTH_SERVER_CLIENT_SECRET" to "authServerClientSecret".getExt(),
            "TEST_API_GATEWAY_HOST" to "api-gateway:8765",
            "TEST_API_GATEWAY_HOST_EXTERNAL" to "localhost:8765",
            "TEST_MOCK_SERVER_HOST" to "mockserver:1080",
            "TEST_EMPLOYEE_SERVICE_HOST" to "employee-service:8080"
        )
    )
}

tasks {
    val migrateMockData by registering(MigrateMockData::class)
}

sourceSets {
    create("ft") {
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
    }
}

val ftImplementation: Configuration by configurations.getting {
    extendsFrom(configurations.implementation.get())
}

ftImplementation.isCanBeResolved = true

configurations["ftRuntimeOnly"].extendsFrom(configurations.runtimeOnly.get())

repositories {
    mavenCentral()
}

ocTemplate {
    workDir.set(layout.buildDirectory.dir("okd"))
    clusterDomain.set("okdClusterDomain".getExt())
    namespace.set("okdProject".getExt())
    prefix.set("employee-ft")

    "okdWebConsoleUrl".getExt().takeIf { it.isNotBlank() }?.let{
        webConsoleUrl.set(it)
    }

    service("mockserver") {
        templateFile.set(rootProject.layout.projectDirectory.file("okd/mockserver.yaml"))
        parameters.set(mapOf(
            "DOCKER_REGISTRY" to "dockerRegistry".getExt(),
            "ACTIVE_DEADLINE_SECONDS" to "okdActiveDeadlineSeconds".getExt(),
            "MOCK_SERVER_VERSION" to properties["mockserver.version"] as String
        ))
    }

    service("gateway") {
        templateFile.set(rootProject.layout.projectDirectory.file("okd/api-gateway.yaml"))
        parameters.set(mapOf(
            "OCTOPUS_GITHUB_DOCKER_REGISTRY" to "octopusGithubDockerRegistry".getExt(),
            "ACTIVE_DEADLINE_SECONDS" to "okdActiveDeadlineSeconds".getExt(),
            "APPLICATION_FT_CONTENT" to layout.projectDirectory.dir("docker/api-gateway.yaml").asFile.readText(),
            "API_GATEWAY_VERSION" to properties["api-gateway.version"] as String,
            "AUTH_SERVER_URL" to "authServerUrl".getExt(),
            "AUTH_SERVER_REALM" to "authServerRealm".getExt(),
            "AUTH_SERVER_CLIENT_ID" to "authServerClientId".getExt(),
            "AUTH_SERVER_CLIENT_SECRET" to "authServerClientSecret".getExt(),
            "TEST_EMPLOYEE_SERVICE_HOST" to getOkdExternalHost("employee"),
            "TEST_API_GATEWAY_HOST_EXTERNAL" to getOkdExternalHost("gateway")
        ))
    }

    service("employee") {
        templateFile.set(rootProject.layout.projectDirectory.file("okd/employee-service.yaml"))
        parameters.set(mapOf(
            "OCTOPUS_GITHUB_DOCKER_REGISTRY" to "octopusGithubDockerRegistry".getExt(),
            "ACTIVE_DEADLINE_SECONDS" to "okdActiveDeadlineSeconds".getExt(),
            "APPLICATION_FT_CONTENT" to layout.projectDirectory.dir("docker/employee-service.yaml").asFile.readText(),
            "EMPLOYEE_SERVICE_VERSION" to project.version as String,
            "AUTH_SERVER_URL" to "authServerUrl".getExt(),
            "AUTH_SERVER_REALM" to "authServerRealm".getExt(),
            "TEST_API_GATEWAY_HOST" to getOkdExternalHost("gateway"),
            "TEST_MOCK_SERVER_HOST" to getOkdExternalHost("mockserver")
        ))
    }
}

tasks.named<MigrateMockData>("migrateMockData") {
    testDataDir.set(rootDir.toString() + File.separator + "test-data")
    when ("testPlatform".getExt()) {
        "okd" -> {
            host.set(ocTemplate.getOkdHost("mockserver"))
            port.set(80)
            dependsOn("ocCreate")
        }
        "docker" -> {
            host.set("localhost")
            port.set(1080)
            dependsOn("composeUp")
        }
    }
}

tasks.named("composeUp") {
    dependsOn(":employee-service:dockerBuildImage")
}

tasks.named("ocCreate") {
    dependsOn(":employee-service:dockerPushImage")
}

val ft by tasks.creating(Test::class) {
    group = "verification"
    description = "Runs the integration tests"
    testClassesDirs = sourceSets["ft"].output.classesDirs
    classpath = sourceSets["ft"].runtimeClasspath
    when ("testPlatform".getExt()) {
        "okd" -> {
            systemProperties["test.api-gateway-host"] = ocTemplate.getOkdHost("gateway")
            dependsOn("migrateMockData")
            ocTemplate.isRequiredBy(this)
        }
        "docker" -> {
            systemProperties["test.api-gateway-host"] = "localhost:8765"
            dependsOn("migrateMockData")
            dockerCompose.isRequiredBy(this)
        }
    }
    systemProperties.putAll(
        mapOf(
            "buildVersion" to project.version,
            "employee-service.user" to "employeeServiceUser".getExt(),
            "employee-service.password" to "employeeServicePassword".getExt()
        )
    )
}

idea.module {
    scopes["PROVIDED"]?.get("plus")?.add(configurations["ftImplementation"])
}

dependencies {
    ftImplementation(project(":client"))
    ftImplementation(project(":common"))
    ftImplementation(project(":test-common"))
    ftImplementation("org.junit.jupiter:junit-jupiter-engine:${project.properties["junit-jupiter.version"]}")
    ftImplementation("org.junit.jupiter:junit-jupiter-params:${project.properties["junit-jupiter.version"]}")
    ftImplementation("com.fasterxml.jackson.core:jackson-core")
    ftImplementation("com.fasterxml.jackson.core:jackson-databind:${project.properties["jackson.version"]}")
    ftImplementation("com.fasterxml.jackson.module:jackson-module-kotlin:${project.properties["jackson.version"]}")

    ftImplementation("ch.qos.logback:logback-core:1.4.1")
    ftImplementation("ch.qos.logback:logback-classic:1.4.1")
    ftImplementation("org.slf4j:slf4j-api:1.7.30")
}