import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.octopusden.task.MigrateMockData
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    idea
    id("org.jetbrains.kotlin.jvm") apply (false)
    id("io.github.gradle-nexus.publish-plugin")
    signing
}

allprojects {
    group = "org.octopusden.octopus.employee"
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
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(11))
        }
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
