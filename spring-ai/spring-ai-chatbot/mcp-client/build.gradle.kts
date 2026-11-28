plugins {
    id("java")
}

description = "AI Chatbot MCP-Client with spring-boot"

dependencies {
    implementation("org.springframework.ai:spring-ai-starter-model-ollama")
    // implementation("org.springframework.ai:spring-ai-starter-model-openai")
    implementation("org.springframework.ai:spring-ai-starter-mcp-client-webflux")
    implementation("org.springframework.boot:spring-boot-starter-web")

    // For Documents
    implementation("org.springframework.ai:spring-ai-tika-document-reader")
    implementation("org.springframework.ai:spring-ai-vector-store-advisor")

    // implementation("org.springframework.ai:spring-ai-model-chat-memory-repository-jdbc")

    // Starter not in embedded Mode.
    // implementation("org.springframework.ai:spring-ai-starter-model-chat-memory-repository-neo4j")
    implementation("org.springframework.ai:spring-ai-model-chat-memory-repository-neo4j")

    // Starter not in embedded Mode.
    // implementation("org.springframework.ai:spring-ai-starter-vector-store-neo4j")
    implementation("org.springframework.ai:spring-ai-neo4j-store")

    // Neo4J embedded.
    implementation("org.neo4j:neo4j") {
        exclude(module = "neo4j-slf4j-provider")
        exclude(module = "commons-logging")
    }

    // runtimeOnly("org.springframework.boot:spring-boot-devtools")
    runtimeOnly("org.springframework.boot:spring-boot-starter-actuator")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<Test>().configureEach {
    // Requires running mcp-server.
    isEnabled = false
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
