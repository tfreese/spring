plugins {
    id("java")
    id("org.springframework.boot")
}

description = "EUREKA-Server (ServiceDiscovery) von spring-boot"

dependencies {
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-server")

    runtimeOnly("de.codecentric:spring-boot-admin-starter-client")
}

// Start: gradle bootRun --args="--spring.profiles.active=dev"
springBoot {
    mainClass.set("de.freese.spring.cloud.eureka.EurekaServerApplication")
}

tasks.named<ProcessResources>("processResources") {
    val map = mapOf(
        "project_description" to (project.description ?: ""), "project_artifactId" to project.name, "project_version" to project.version.toString()
    )

    filesMatching("application.yml") {
        filter(
            mapOf("tokens" to map), org.apache.tools.ant.filters.ReplaceTokens::class.java
        )
    }
}
