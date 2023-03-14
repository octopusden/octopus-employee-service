import org.octopusden.task.MigrateMockData
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    idea
    id("org.octopusden.release-management")
    id("org.jetbrains.kotlin.jvm") apply (false)
    id("io.github.gradle-nexus.publish-plugin")
    signing
}

autoUpdateDependencies {
    component(mapOf("name" to "cloud-commons", "projectProperty" to "cloud-commons.version"))
}

allprojects {
    group = "org.octopusden.employee"
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
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
        implementation(platform("com.fasterxml.jackson:jackson-bom:2.12.3"))
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    }

    tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions {
            suppressWarnings = true
            jvmTarget = "1.8"
        }
    }

    val migrateMockData by tasks.creating(MigrateMockData::class) {
        this.testDataDir = rootDir.toString() + File.separator + "test-data"
    }
}

tasks.findByName("publish")?.dependsOn(":employee-service:dockerPushImage")