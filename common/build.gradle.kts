import org.gradle.jvm.toolchain.JavaLanguageVersion

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
    sign(publishing.publications["maven"])
}

java {
    withJavadocJar()
    withSourcesJar()
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-annotations")
    api(platform("io.github.openfeign:feign-bom:11.9.1"))
    api("io.github.openfeign:feign-httpclient")
    api("io.github.openfeign:feign-jackson")
    api("io.github.openfeign:feign-slf4j")
    api("org.apache.httpcomponents:httpclient:4.5.13")
}
