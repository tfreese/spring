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
    val map = mapOf(
        "project_description" to (description ?: ""), "project_artifactId" to name, "project_version" to version.toString()
    )

    filesMatching("application.yml") {
        filter(
            mapOf("tokens" to map), org.apache.tools.ant.filters.ReplaceTokens::class.java
        )
    }
}
