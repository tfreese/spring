plugins {
    id("java")
    id("org.springframework.boot")
}

description = "Demo für spring-boot-web mit JSF"

// Variante 1.
// Global aus allen Configurations entfernen.
configurations.configureEach {
    if (name != "mockitoAgent") {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
    }
}

dependencies {
    // Variante 2.
    // "org.springframework.boot:spring-boot-starter-logging" ist immer noch als Dependency in der Gradle-View zu sehen, wird aber nicht verwendet.
    // modules {
    //     module("org.springframework.boot:spring-boot-starter-logging") {
    //         replacedBy("org.springframework.boot:spring-boot-starter-log4j2", "Use Log4j2 instead of Logback")
    //     }
    // }

    implementation("org.slf4j:slf4j-api")

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

    implementation("org.springframework.boot:spring-boot-starter-webmvc") {
        //     // Variante 3, nur für DIESE Dependency!
        //     // "spring-boot-starter-logging" ist nun auch nicht mehr in der Gradle-View zu sehen.
        //     exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
    }

    implementation("software.xdev:chartjs-java-model")

    implementation("org.springframework.boot:spring-boot-starter-opentelemetry")
    // Logging-EndPoint
    implementation("io.opentelemetry:opentelemetry-exporter-logging-otlp")
    // Eigener EndPoint
    implementation("io.opentelemetry.proto:opentelemetry-proto")

    // runtimeOnly("org.springframework.boot:spring-boot-devtools")
    runtimeOnly("org.springframework.boot:spring-boot-starter-actuator")
    runtimeOnly("org.springframework.boot:spring-boot-starter-log4j2")

    // Nur fuer lokale Entwicklung (nicht im produktiven Fat-Jar enthalten):
    // automatischer Neustart bei Java-Aenderungen + LiveReload-Browser-Refresh.
    // XHTML-Aenderungen loesen dank joinfaces.faces.facelets-refresh-period=0
    // (application.properties) ohnehin KEINEN Neustart aus - nur den LiveReload.
    developmentOnly("org.springframework.boot:spring-boot-devtools")
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
    description = "Copy all runtime dependencies to build/libs"

    println("build " + project.name)

    into(layout.buildDirectory)

    into("libs") {
        from(configurations.runtimeClasspath)
    }
}
