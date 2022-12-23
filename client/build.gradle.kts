plugins {
    `maven-publish`
}

publishing {
    repositories {
        maven {

        }
    }
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}

signing {
    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications["maven"])
}

java {
    withJavadocJar()
    withSourcesJar()
}

dependencies {
    api(project(":common"))
    api("com.fasterxml.jackson.module:jackson-module-kotlin")
    api("com.fasterxml.jackson.core:jackson-databind")
}
