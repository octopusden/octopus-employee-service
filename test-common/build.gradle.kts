dependencies {
    implementation(project(":common"))
    implementation("org.junit.jupiter:junit-jupiter-engine:${project.properties["junit-jupiter.version"]}")
    implementation("org.junit.jupiter:junit-jupiter-params:${project.properties["junit-jupiter.version"]}")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.16.1")
}
