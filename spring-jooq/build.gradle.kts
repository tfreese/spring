plugins {
    id("java")
}

description = "JOOQ-Demo"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-jooq")
    // implementation("org.springframework.boot:spring-boot-starter-webmvc")

    implementation("org.jooq:jooq-codegen")

    // runtimeOnly("org.springframework.boot:spring-boot-h2console")
    runtimeOnly("com.h2database:h2")
}
