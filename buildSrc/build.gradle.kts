plugins {
    kotlin("jvm") version "1.9.22"
    groovy
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(gradleApi())
    implementation("org.mock-server:mockserver-client-java:5.11.1")
}
