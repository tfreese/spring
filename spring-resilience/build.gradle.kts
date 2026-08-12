plugins {
    id("java")
    id("org.springframework.boot")
}

description = "Demo project for Spring Boot"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    // implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.cloud:spring-cloud-starter-circuitbreaker-reactor-resilience4j")

    // runtimeOnly("de.codecentric:spring-boot-admin-starter-client")
    runtimeOnly("io.micrometer:micrometer-registry-prometheus")
    runtimeOnly("org.springframework.boot:spring-boot-starter-actuator")

    testImplementation("io.github.resilience4j:resilience4j-all")
    // testImplementation("io.projectreactor:reactor-test")
    // implementation("org.springframework.boot:spring-boot-starter-webmvc-test")
}

// Start: gradle bootRun --args="--spring.profiles.active=dev"
springBoot {
    mainClass.set("de.freese.spring.resilience.ResilienceApplication")
}

// The archive name. If the name has not been explicitly set, the pattern for the name is:
// [archiveBaseName]-[archiveAppendix]-[archiveVersion]-[archiveClassifier].[archiveExtension]
tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    archiveFileName.set("${archiveBaseName.get()}-boot.${archiveExtension.get()}")
}

tasks.named<ProcessResources>("processResources") {
    val map = mapOf(
        "project_description" to (project.description ?: ""), "project_artifactId" to project.name, "project_version" to project.version.toString()
    )

    filesMatching(listOf("application.yml", "bootstrap.yml")) {
        filter(
            mapOf("tokens" to map), org.apache.tools.ant.filters.ReplaceTokens::class.java
        )
    }
}
