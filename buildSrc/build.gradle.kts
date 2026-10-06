plugins {
    // The Kotlin that Gradle itself embeds: this code compiles against, and runs inside, Gradle's
    // own API and stdlib, which an older compiler cannot read.
    kotlin("jvm") version embeddedKotlinVersion
    groovy
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(gradleApi())
    implementation("org.mock-server:mockserver-client-java:5.11.1")
}
