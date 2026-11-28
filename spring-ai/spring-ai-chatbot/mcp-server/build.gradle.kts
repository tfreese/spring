plugins {
    id("java")
}

description = "AI Chatbot MCP-Server with spring-boot"

dependencies {
    // implementation("org.springframework.ai:spring-ai-starter-mcp-server-webflux")
    implementation("org.springframework.ai:spring-ai-starter-mcp-server-webmvc")

    // runtimeOnly("org.springframework.boot:spring-boot-devtools")
    runtimeOnly("org.springframework.boot:spring-boot-starter-actuator")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.named<ProcessResources>("processResources") {
    val projectDescription = project.description ?: ""
    val projectArtifactId = project.name
    val projectVersion = project.version.toString()

    filteringCharset = "UTF-8"

    filesMatching(listOf("application*.yml", "application*.yaml", "application*.properties", "bootstrap.yml")) {
        filter { line: String ->
            line
                .replace("@project_description@", projectDescription)
                .replace("@project_artifactId@", projectArtifactId)
                .replace("@project_version@", projectVersion)
        }
    }
}
