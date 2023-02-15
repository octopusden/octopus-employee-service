plugins {
    id("com.avast.gradle.docker-compose") version "0.14.3"
}

val dockerRegistry = System.getenv().getOrDefault("DOCKER_REGISTRY", project.properties["docker.registry"]) as? String
val octopusGithubDockerRegistry = System.getenv().getOrDefault("OCTOPUS_GITHUB_DOCKER_REGISTRY", project.properties["octopus.github.docker.registry"]) as? String
val authServerUrl = System.getenv().getOrDefault("AUTH_SERVER_URL", project.properties["auth-server.url"]) as? String
val authServerRealm = System.getenv().getOrDefault("AUTH_SERVER_REALM", project.properties["auth-server.realm"]) as? String
val authServerClientId = System.getenv().getOrDefault("AUTH_SERVER_CLIENT_ID", project.properties["auth-server.client-id"]) as? String
val authServerClientSecret = System.getenv().getOrDefault("AUTH_SERVER_CLIENT_SECRET", project.properties["auth-server.client-secret"]) as? String
val employeeServiceUser = System.getenv().getOrDefault("EMPLOYEE_SERVICE_USER", project.properties["employee-service.user"]) as? String
val employeeServicePassword = System.getenv().getOrDefault("EMPLOYEE_SERVICE_PASSWORD", project.properties["employee-service.password"]) as? String

dockerCompose {
    useComposeFiles.add("${projectDir}${File.separator}docker${File.separator}docker-compose.yml")
    waitForTcpPorts = true
    captureContainersOutputToFiles = File("$buildDir${File.separator}docker_logs")
    environment.putAll(
        mapOf(
            "APP_VERSION" to project.version,
            "DOCKER_REGISTRY" to dockerRegistry,
            "OCTOPUS_GITHUB_DOCKER_REGISTRY" to octopusGithubDockerRegistry,
            "AUTH_SERVER_URL" to authServerUrl,
            "AUTH_SERVER_REALM" to authServerRealm,
            "AUTH_SERVER_CLIENT_ID" to authServerClientId,
            "AUTH_SERVER_CLIENT_SECRET" to authServerClientSecret
        )
    )
}

tasks.getByName("composeUp").doFirst {
    if (dockerRegistry.isNullOrBlank() || octopusGithubDockerRegistry.isNullOrBlank() ||
        authServerUrl.isNullOrBlank() || authServerRealm.isNullOrBlank() ||
        authServerClientId.isNullOrBlank() || authServerClientSecret.isNullOrBlank()
    ) {
        throw IllegalArgumentException(
            "Start gradle build with" +
                    (if (dockerRegistry.isNullOrBlank()) " -Pdocker.registry=..." else "") +
                    (if (octopusGithubDockerRegistry.isNullOrBlank()) " -Poctopus.github.docker.registry=..." else "") +
                    (if (authServerUrl.isNullOrBlank()) " -Pauth-server.url=..." else "") +
                    (if (authServerRealm.isNullOrBlank()) " -Pauth-server.realm=..." else "") +
                    (if (authServerClientId.isNullOrBlank()) " -Pauth-server.client-id=..." else "") +
                    (if (authServerClientSecret.isNullOrBlank()) " -Pauth-server.client-secret=..." else "") +
                    " or set env variable(s):" +
                    (if (dockerRegistry.isNullOrBlank()) " DOCKER_REGISTRY" else "") +
                    (if (octopusGithubDockerRegistry.isNullOrBlank()) " OCTOPUS_GITHUB_DOCKER_REGISTRY" else "") +
                    (if (authServerUrl.isNullOrBlank()) " AUTH_SERVER_URL" else "") +
                    (if (authServerRealm.isNullOrBlank()) " AUTH_SERVER_REALM" else "") +
                    (if (authServerClientId.isNullOrBlank()) " AUTH_SERVER_CLIENT_ID" else "") +
                    (if (authServerClientSecret.isNullOrBlank()) " AUTH_SERVER_CLIENT_SECRET" else "")
        )
    }
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

val ft by tasks.creating(Test::class) {
    doFirst {
        if (employeeServiceUser.isNullOrBlank() || employeeServicePassword.isNullOrBlank()) {
            throw IllegalArgumentException(
                "Start gradle build with" +
                        (if (employeeServiceUser.isNullOrBlank()) " -Pemployee-service.user=..." else "") +
                        (if (employeeServicePassword.isNullOrBlank()) " -Pemployee-service.password=..." else "") +
                        " or set env variable(s):" +
                        (if (employeeServiceUser.isNullOrBlank()) " EMPLOYEE_SERVICE_USER" else "") +
                        (if (employeeServicePassword.isNullOrBlank()) " EMPLOYEE_SERVICE_PASSWORD" else "")
            )
        }
    }

    systemProperties.putAll(
        mapOf(
            "buildVersion" to project.version,
            "employee-service.user" to employeeServiceUser,
            "employee-service.password" to employeeServicePassword
        )
    )
    group = "verification"
    description = "Runs the integration tests"
    testClassesDirs = sourceSets["ft"].output.classesDirs
    classpath = sourceSets["ft"].runtimeClasspath
}

dockerCompose.isRequiredBy(ft)

tasks.named("composeUp") {
    dependsOn(":employee-service:dockerBuildImage")
}

tasks.named("migrateMockData") {
    dependsOn("composeUp")
}

tasks.named("ft") {
    dependsOn("migrateMockData")
}

repositories {
    mavenCentral()
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
    ftImplementation("com.fasterxml.jackson.core:jackson-databind")
    ftImplementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    ftImplementation("ch.qos.logback:logback-core:1.4.1")
    ftImplementation("ch.qos.logback:logback-classic:1.4.1")
    ftImplementation("org.slf4j:slf4j-api:1.7.30")
}
