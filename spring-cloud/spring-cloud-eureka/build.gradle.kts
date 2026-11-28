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
