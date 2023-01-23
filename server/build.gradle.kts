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
    id("com.bmuschko.docker-spring-boot-application") version "6.4.0"
    `maven-publish`
    id("com.avast.gradle.docker-compose") version "0.14.3"
}

repositories {
    mavenCentral()
}

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
        baseImage.set("${rootProject.properties["docker.registry"]}/openjdk:11")
        ports.set(listOf(8080, 8080))
        images.set(setOf("${rootProject.properties["publishing.docker.registry"]}/${project.name}:${project.version}"))
    }
}

dockerCompose {
    useComposeFiles.add("${projectDir}/docker/docker-compose.yml")
    waitForTcpPorts = true

    (System.getenv().get("DOCKER_REGISTRY") ?: project.properties["docker.registry"])
        ?.let {
            environment["DOCKER_REGISTRY"] = it
        }
    (System.getenv().get("PUBLISHING_DOCKER_REGISTRY") ?: project.properties["publishing.docker.registry"])
        ?.let {
            environment["PUBLISHING_DOCKER_REGISTRY"] = it
        }
    captureContainersOutputToFiles = File("$buildDir${File.separator}/docker_logs")
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
}

dockerCompose.isRequiredBy(tasks["test"])

dependencies {
    implementation(project(":common"))

    implementation(platform("org.springframework.cloud:spring-cloud-dependencies:${project.properties["spring-cloud.version"]}"))
    implementation(platform("org.springframework.boot:spring-boot-dependencies:${project.properties["spring-boot.version"]}"))

    implementation("org.springframework.cloud:spring-cloud-starter-config")
    implementation("org.springframework.cloud:spring-cloud-starter-bootstrap")
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
    implementation("io.micrometer:micrometer-registry-prometheus:1.9.5")

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.retry:spring-retry")
    implementation("org.springframework.boot:spring-boot-starter-aop")

    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.security:spring-security-oauth2-resource-server")
    implementation("org.springframework.security:spring-security-oauth2-jose")
    implementation("org.octopusden.octopus-cloud-commons:octopus-security-common:${project.properties["cloud-commons.version"]}")

    implementation("io.springfox:springfox-boot-starter:3.0.0")
    implementation("io.springfox:springfox-swagger-ui:3.0.0")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation(project(":test-common"))
}
