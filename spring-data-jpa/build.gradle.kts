plugins {
    id("java")
}

description = "spring-data-jpa"

dependencies {
    // implementation("tools.jackson.module:jackson-module-jaxb-annotations")
    // implementation("tools.jackson.module:jackson-module-jakarta-xmlbind-annotations")
    implementation("jakarta.xml.bind:jakarta.xml.bind-api")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")

    runtimeOnly("tools.jackson.dataformat:jackson-dataformat-xml")

    runtimeOnly("org.springframework.boot:spring-boot-h2console")
    // runtimeOnly("com.h2database:h2")

    // runtimeOnly("org.glassfish.jaxb:jaxb-runtime") // Implementation of jakarta.json.bind:jakarta.json.bind-api
    // runtimeOnly("org.hsqldb:hsqldb")

    testImplementation("org.apache.httpcomponents.client5:httpclient5")
    testImplementation("org.springframework.boot:spring-boot-starter-restclient")
    testImplementation("org.springframework.boot:spring-boot-starter-webclient")
    testImplementation("org.springframework.boot:spring-boot-webtestclient")
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
