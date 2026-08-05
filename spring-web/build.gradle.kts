plugins {
    id("java")
    id("org.springframework.boot")
}

description = "Demo für spring-boot-web mit JSF"

dependencies {
    modules {
        module("org.springframework.boot:spring-boot-starter-logging") {
            replacedBy("org.springframework.boot:spring-boot-starter-log4j2", "Use Log4j2 instead of Logback")
        }
    }

    // With Joinfaces: 77 Dependencies and 41,81MB.
    implementation("org.joinfaces:primefaces-spring-boot-starter")

    // Without Joinfaces: 73 Dependencies and 45,41MB.
    // implementation("org.glassfish:jakarta.faces")
    // implementation("jakarta.faces:jakarta.faces-api")
    // implementation("org.jboss.weld.servlet:weld-servlet-core") // CDI
    // runtimeOnly("org.apache.tomcat.embed:tomcat-embed-jasper")
    // runtimeOnly("org.primefaces:primefaces::jakarta")
    // runtimeOnly("org.primefaces:primefaces-themes")
    // runtimeOnly("org.primefaces.extensions:primefaces-extensions::jakarta")

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("software.xdev:chartjs-java-model")

    implementation("org.springframework.boot:spring-boot-starter-opentelemetry")
// Logging-EndPoint
    implementation("io.opentelemetry:opentelemetry-exporter-logging-otlp")
// Eigener EndPoint
    implementation("io.opentelemetry.proto:opentelemetry-proto:1.7.0-alpha")

    // runtimeOnly("org.springframework.boot:spring-boot-devtools")
    runtimeOnly("org.springframework.boot:spring-boot-starter-actuator")
    runtimeOnly("org.springframework.boot:spring-boot-starter-log4j2")
}

// Start: gradle bootRun --args="--spring.profiles.active=dev"
springBoot {
    mainClass.set("de.freese.spring.web.SpringBootWebApplication")
}
// tasks.named("bootJar") {
//     layered {
//         enabled = false
//     }
// }

sourceSets {
    main {
        resources {
            srcDir(layout.projectDirectory.dir("src").dir("main").dir("webapp"))
        }
    }
}

tasks.named<ProcessResources>("processResources") {
    val map = mapOf(
        "project_description" to project.description, "project_artifactId" to project.name, "project_version" to project.version
    )

    filesMatching("application.yml") {
        // expand(map)
        filter(
            mapOf("tokens" to map), org.apache.tools.ant.filters.ReplaceTokens::class.java
        )
    }
}

tasks.register<Copy>("copyLibs") {
    group = "spring-web"

    println("build " + project.name)

    into(layout.buildDirectory)

    into("libs") {
        from(configurations.runtimeClasspath)
    }
}
