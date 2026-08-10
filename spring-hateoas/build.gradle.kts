plugins {
    id("java")
    id("org.springframework.boot")
}

description = "Demo für REST-HATEOAS"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-hateoas")
    implementation("org.springframework.boot:spring-boot-starter-webflux")

//        runtimeOnly("org.springframework.boot:spring-boot-starter-actuator")

//        testImplementation("com.jayway.jsonpath:json-path")

    testImplementation("org.springframework.boot:spring-boot-starter-restclient")
    testImplementation("org.springframework.boot:spring-boot-starter-webflux-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
}

// Start: gradle bootRun --args="--spring.profiles.active=dev"
springBoot {
    mainClass.set("de.freese.spring.hateoas.HateoasApplication")
}

tasks.named<ProcessResources>("processResources") {
    val map = mapOf(
        "project_description" to (description ?: ""), "project_artifactId" to name, "project_version" to version.toString()
    )

    filesMatching(listOf("application*.yml", "application*.yaml", "application*.properties")) {
        filter(
            mapOf("tokens" to map), org.apache.tools.ant.filters.ReplaceTokens::class.java
        )
    }
}
