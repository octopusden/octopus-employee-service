plugins {
    id("com.avast.gradle.docker-compose") version "0.14.3"
}

dockerCompose {
    useComposeFiles.add("${projectDir}${File.separator}docker${File.separator}docker-compose.yml")
    waitForTcpPorts = true

    environment["APP_VERSION"] = project.version
    project.properties["docker.registry"]
        ?.let { environment["DOCKER_REGISTRY"] = it }
    project.properties["auth-server.url"]
        ?.let { environment["AUTH_SERVER_URL"] }
    captureContainersOutputToFiles = File("$buildDir${File.separator}docker_logs")
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
    systemProperties = mapOf("buildVersion" to version)
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
