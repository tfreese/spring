plugins {
    id("java")
    id("org.springframework.boot")
}

description = "Demo Project for Kubernetes"

dependencies {
    // implementation("io.r2dbc:r2dbc-h2")
    // implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    // runtimeOnly("org.mariadb.jdbc:mariadb-java-client")
    runtimeOnly("com.h2database:h2")
    // runtimeOnly("org.hsqldb:hsqldb")

    // testRuntimeOnly("com.h2database:h2")
}

// Start: gradle bootRun --args="--spring.profiles.active=dev"
springBoot {
    mainClass.set("de.freese.kubernetes.backend.MyApplication")
}

// The archive name. If the name has not been explicitly set, the pattern for the name is:
// [archiveBaseName]-[archiveAppendix]-[archiveVersion]-[archiveClassifier].[archiveExtension]
tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    archiveFileName.set("${archiveBaseName.get()}-boot.${archiveExtension.get()}")
}


