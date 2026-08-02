plugins {
    id("java")
    id("org.springframework.boot")
}

description = "Monitor für Spring-Boot Anwendungen"

dependencies {
    implementation("de.codecentric:spring-boot-admin-starter-server")
}

// Start: gradle bootRun --args="--spring.profiles.active=dev"
springBoot {
    mainClass.set("de.freese.spring.boot.cloud.admin.SpringBootAdminApplication")
}
