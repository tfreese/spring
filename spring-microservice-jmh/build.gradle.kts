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
    val map = mapOf(
        "project_description" to (project.description ?: ""), "project_artifactId" to project.name, "project_version" to project.version.toString()
    )

    filesMatching(listOf("application*.yml", "bootstrap.yml")) {
        filter(
            mapOf("tokens" to map), org.apache.tools.ant.filters.ReplaceTokens::class.java
        )
    }
}
