import org.gradle.kotlin.dsl.withType
import org.octopusden.task.MigrateMockData

plugins {
    id("org.springframework.boot")
    id("org.jetbrains.kotlin.plugin.spring")
    id("org.jetbrains.kotlin.plugin.jpa")
    id("org.jetbrains.kotlin.plugin.allopen")
    id("org.jetbrains.kotlin.plugin.noarg")
    id("org.octopusden.octopus.oc-template")
    id("com.bmuschko.docker-spring-boot-application") version "9.4.0"
    `maven-publish`
    id("com.avast.gradle.docker-compose") version "0.16.9"
}

repositories {
    mavenCentral()
}

fun String.getExt() = project.ext[this] as String

tasks.getByName<Jar>("jar") {
    enabled = false
}

publishing {
    publications {
        create<MavenPublication>("bootJar") {
            artifact(tasks.getByName("bootJar"))
            from(components["java"])
            pom {
                name.set(project.name)
                description.set("Octopus module: ${project.name}")
                url.set("https://github.com/octopusden/octopus-employee-service.git")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                scm {
                    url.set("https://github.com/kzaporozhtsev/octopus-employee-service.git")
                    connection.set("scm:git://github.com/octopusden/octopus-employee-service.git")
                }
                developers {
                    developer {
                        id.set("octopus")
                        name.set("octopus")
                    }
                }
            }
        }
    }
}

signing {
    isRequired = project.ext["signingRequired"] as Boolean
    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications["bootJar"])
}

tasks {
    val migrateMockData by registering(MigrateMockData::class)
}

springBoot {
    buildInfo()
}

docker {
    springBootApplication {
        baseImage.set("${"dockerRegistry".getExt()}/eclipse-temurin:21-jdk")
        ports.set(listOf(8080))
        images.set(setOf("${"octopusGithubDockerRegistry".getExt()}/octopusden/${project.name}:${project.version}"))
    }
}

tasks.getByName("dockerPushImage") {
    dependsOn("dockerBuildImage")
}

dockerCompose {
    useComposeFiles.add("${projectDir}/docker/docker-compose.yml")
    waitForTcpPorts = true
    captureContainersOutputToFiles = layout.buildDirectory.dir("docker-logs").get().asFile
    environment.putAll(mapOf("DOCKER_REGISTRY" to "dockerRegistry".getExt(), "MOCK_SERVER_VERSION" to properties["mockserver.version"] as String))
}


sourceSets {
    test {
        resources {
            srcDir(project.rootDir.toString() + File.separator + "test-data")
        }
    }
}

ocTemplate {
    workDir.set(layout.buildDirectory.dir("okd"))
    clusterDomain.set("okdClusterDomain".getExt())
    namespace.set("okdProject".getExt())
    prefix.set("employee-ut")

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

tasks.withType<Test> {
    when ("testPlatform".getExt()) {
        "okd" -> {
            systemProperties["test.mockserver-host"] = ocTemplate.getOkdHost("mockserver")
            dependsOn("migrateMockData")
            ocTemplate.isRequiredBy(this)
        }
        "docker" -> {
            systemProperties["test.mockserver-host"] = "localhost:1080"
            dependsOn("migrateMockData")
            dockerCompose.isRequiredBy(this)
        }
    }
    environment.putAll(mapOf("AUTH_SERVER_URL" to "authServerUrl".getExt(), "AUTH_SERVER_REALM" to "authServerRealm".getExt()))
}

dependencies {
    implementation(project(":common"))

    implementation(platform("org.springframework.boot:spring-boot-dependencies:${project.properties["spring-boot.version"]}"))
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.retry:spring-retry")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.security:spring-security-oauth2-resource-server")
    implementation("org.springframework.security:spring-security-oauth2-jose")
    implementation("org.octopusden.octopus-cloud-commons:octopus-security-common:${project.properties["octopus-cloud-commons.version"]}")

    implementation(platform("org.springframework.cloud:spring-cloud-dependencies:${project.properties["spring-cloud.version"]}"))
    implementation("org.springframework.cloud:spring-cloud-starter-config")
    implementation("org.springframework.cloud:spring-cloud-starter-bootstrap")
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
    implementation("io.micrometer:micrometer-registry-prometheus")

    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:${project.properties["springdoc.version"]}")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:${project.properties["jackson.version"]}")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation(project(":test-common"))
}