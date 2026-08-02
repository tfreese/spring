plugins {
    id("java")
}

description = "Multiple Spring-Contexts"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
}

tasks.named<ProcessResources>("processResources") {
    val map = mapOf(
        "project_description" to project.description, "project_artifactId" to project.name, "project_version" to project.version
    )

    filesMatching(listOf("application-*.yml", "bootstrap.yml")) {
        // expand(map)
        filter(
            mapOf("tokens" to map), org.apache.tools.ant.filters.ReplaceTokens::class.java
        )
    }
}
