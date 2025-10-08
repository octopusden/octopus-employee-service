import org.gradle.kotlin.dsl.withType
import org.octopusden.task.MigrateMockData

buildscript {
    dependencies {
        classpath("com.bmuschko:gradle-docker-plugin:3.6.2")
    }
}

plugins {
    id("org.springframework.boot")
    id("org.jetbrains.kotlin.plugin.spring")
    id("org.jetbrains.kotlin.plugin.jpa")
    id("org.jetbrains.kotlin.plugin.allopen")
    id("org.jetbrains.kotlin.plugin.noarg")
    id("com.bmuschko.docker-spring-boot-application") version "9.4.0"
    `maven-publish`
    id("com.avast.gradle.docker-compose") version "0.16.9"
}

repositories {
    mavenCentral()
}

val dockerRegistry = System.getenv().getOrDefault("DOCKER_REGISTRY", project.properties["docker.registry"]) as? String
val octopusGithubDockerRegistry = System.getenv().getOrDefault("OCTOPUS_GITHUB_DOCKER_REGISTRY", project.properties["octopus.github.docker.registry"]) as? String
val authServerUrl = System.getenv().getOrDefault("AUTH_SERVER_URL", project.properties["auth-server.url"]) as? String
val authServerRealm = System.getenv().getOrDefault("AUTH_SERVER_REALM", project.properties["auth-server.realm"]) as? String

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
    isRequired = System.getenv().containsKey("ORG_GRADLE_PROJECT_signingKey") && System.getenv().containsKey("ORG_GRADLE_PROJECT_signingPassword")
    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications["bootJar"])
}

springBoot {
    buildInfo()
}

docker {
    springBootApplication {
        baseImage.set("$dockerRegistry/eclipse-temurin:21-jdk")
        ports.set(listOf(8080))
        images.set(setOf("$octopusGithubDockerRegistry/octopusden/${project.name}:${project.version}"))
    }
}

tasks.getByName("dockerBuildImage").doFirst {
    if (dockerRegistry.isNullOrBlank() || octopusGithubDockerRegistry.isNullOrBlank()) {
        throw IllegalArgumentException(
            "Start gradle build with" +
                    (if (dockerRegistry.isNullOrBlank()) " -Pdocker.registry=..." else "") +
                    (if (octopusGithubDockerRegistry.isNullOrBlank()) " -Poctopus.github.docker.registry=..." else "") +
                    " or set env variable(s):" +
                    (if (dockerRegistry.isNullOrBlank()) " DOCKER_REGISTRY" else "") +
                    (if (octopusGithubDockerRegistry.isNullOrBlank()) " OCTOPUS_GITHUB_DOCKER_REGISTRY" else "")
        )
    }
}

dockerCompose {
    useComposeFiles.add("${projectDir}/docker/docker-compose.yml")
    waitForTcpPorts = true
    captureContainersOutputToFiles = layout.buildDirectory.dir("docker_logs").get().asFile
    environment.putAll(mapOf("DOCKER_REGISTRY" to dockerRegistry))
}

tasks.getByName("composeUp").doFirst {
    if (dockerRegistry.isNullOrBlank()) throw IllegalArgumentException("Start gradle build with -Pdocker.registry=... or set env variable(s): DOCKER_REGISTRY")
}

sourceSets {
    test {
        resources {
            srcDir(project.rootDir.toString() + File.separator + "test-data")
        }
    }
}

tasks.withType<Test> {
    dependsOn("migrateMockData")

    doFirst {
        if (authServerUrl.isNullOrBlank() || authServerRealm.isNullOrBlank()) {
            throw IllegalArgumentException(
                "Start gradle build with" +
                        (if (authServerUrl.isNullOrBlank()) " -Pauth-server.url=..." else "") +
                        (if (authServerRealm.isNullOrBlank()) " -Pauth-server.realm=..." else "") +
                        " or set env variable(s):" +
                        (if (authServerUrl.isNullOrBlank()) " AUTH_SERVER_URL" else "") +
                        (if (authServerRealm.isNullOrBlank()) " AUTH_SERVER_REALM" else "")
            )
        }
    }

    environment.putAll(mapOf("AUTH_SERVER_URL" to authServerUrl, "AUTH_SERVER_REALM" to authServerRealm))
}

tasks.withType<MigrateMockData> {
    dependsOn("composeUp")
}

dockerCompose.isRequiredBy(tasks["test"])

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

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.16.1")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation(project(":test-common"))
}
