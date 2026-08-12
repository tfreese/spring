plugins {
    id("java")
    id("org.springframework.boot")
}

description = "spring-cloud-api-gateway"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.cloud:spring-cloud-gateway-server-webflux")
    implementation("org.springframework.cloud:spring-cloud-starter-loadbalancer")

    runtimeOnly("de.codecentric:spring-boot-admin-starter-client")
    runtimeOnly("org.springframework.boot:spring-boot-starter-actuator")
    runtimeOnly("org.springframework.cloud:spring-cloud-starter-circuitbreaker-reactor-resilience4j")
    runtimeOnly("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
}

// Start: gradle bootRun --args="--spring.profiles.active=dev"
springBoot {
    mainClass.set("de.freese.spring.cloud.gateway.GatewayApplication")
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
