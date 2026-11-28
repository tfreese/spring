plugins {
    id("java")
    id("org.springframework.boot")
}

description = "JMH-Benchmark eines MicroService mit spring-boot"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    testAnnotationProcessor("org.openjdk.jmh:jmh-generator-annprocess")

    testImplementation("org.openjdk.jmh:jmh-core")
    testImplementation("org.springframework.boot:spring-boot-restclient")
    testImplementation("org.springframework.boot:spring-boot-starter-webclient")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
}

// Start: gradle bootRun --args="--spring.profiles.active=dev"
springBoot {
    mainClass.set("de.freese.spring.MicroServiceApplication")
}

// tasks.named<Test>("test") {
//    filter {
//        // JMH-Test deaktivieren, weil das mit den Annotation-Processor nicht klappt.
//        excludeTestsMatching "de.freese.spring.TestRestService.testBenchmark"
//    }
//}

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
