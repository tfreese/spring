plugins {
    id("java")
    id("org.springframework.boot")
}

description = "Spring Boot Web Thymeleaf mit Security Beispiel"

dependencies {
    implementation("tools.jackson.dataformat:jackson-dataformat-xml")

    implementation("org.apache.httpcomponents.client5:httpclient5")
    implementation("org.apache.httpcomponents.core5:httpcore5-reactive")
    implementation("org.springframework.boot:spring-boot-starter-json")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    implementation("org.springframework.boot:spring-boot-starter-restclient")
    implementation("org.springframework.boot:spring-boot-starter-webclient")

    // hot swapping, disable cache for template, enable live reload
    // implementation("org.springframework.boot:spring-boot-devtools")

    implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity6")

    runtimeOnly("org.springframework.boot:spring-boot-starter-actuator")

    // Optional, for bootstrap
    // runtimeOnly("org.webjars:bootstrap:5.3.3")

    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webflux-test")
}

tasks.withType<Test>().configureEach {
    // No parallel Tests to avoid "Address is already in use".
    maxParallelForks = 1

    // Alternative: 'gradle --no-parallel --max-workers 1 test'

    filter {
        // Deactivate Class
        // excludeTestsMatching "PACKAGE.CLASS"

        // Deactivate Method
        // excludeTestsMatching "PACKAGE.CLASS.METHOD"

        // Deactivate Package with Wildcards
        // excludeTestsMatching "*.integration.*"

        // Deactivate Method with Wildcards
        // excludeTestsMatching "*stress*"
    }
}

// Start: gradle bootRun --args="--spring.profiles.active=dev"
springBoot {
    mainClass.set("de.freese.spring.thymeleaf.ThymeleafApplication")
}

tasks.named<ProcessResources>("processResources") {
    val map = mapOf(
        "project_description" to (description ?: ""), "project_artifactId" to name, "project_version" to version.toString()
    )

    filesMatching(listOf("application.yml", "bootstrap.yml")) {
        filter(
            mapOf("tokens" to map), org.apache.tools.ant.filters.ReplaceTokens::class.java
        )
    }
}
