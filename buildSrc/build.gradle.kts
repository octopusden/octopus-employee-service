plugins {
    kotlin("jvm") version "1.6.21"
    groovy
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(gradleApi())
    implementation("org.mock-server:mockserver-client-java:5.11.1")
}
