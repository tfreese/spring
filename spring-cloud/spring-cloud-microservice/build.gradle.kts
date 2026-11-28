plugins {
    id("java")
    id("org.springframework.boot")
}

description = "MicroService mit spring-boot"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")

    runtimeOnly("de.codecentric:spring-boot-admin-starter-client")
    runtimeOnly("com.h2database:h2")
    // runtimeOnly("org.hsqldb:hsqldb")
    runtimeOnly("org.springframework.boot:spring-boot-starter-actuator")

    testImplementation("tools.jackson.dataformat:jackson-dataformat-xml")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
}

// Start: gradle bootRun --args="--spring.profiles.active=dev"
springBoot {
    mainClass.set("de.freese.spring.cloud.microservice.MicroServiceApplication")
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
