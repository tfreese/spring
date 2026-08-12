plugins {
    id("java")
    id("org.springframework.boot")
}

description = "JWT-Demo"

// tasks.named<Delete>("clean") {
//     delete(layout.projectDirectory.dir("src").dir("main").dir("resources").dir("certs"))
// }

dependencies {
    // implementation("com.nimbusds:nimbus-jose-jwt")

    // Older Alternative for nimbus-jose-jwt
    // implementation("io.jsonwebtoken:jjwt")
    // implementation("io.jsonwebtoken:jjwt-impl")

    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework:spring-jdbc")

    // runtimeOnly("org.glassfish.jaxb:jaxb-runtime")

    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.security:spring-security-test")
}

// Start: gradle bootRun --args="--spring.profiles.active=dev"
springBoot {
    mainClass.set("de.freese.spring.jwt.JwtApplication")
}

tasks.register<Exec>("createRsaKeys") {
    group = "myTasks"
    description = "Create RSA Keys"

    val certDir = layout.buildDirectory.dir("resources").get().dir("main").dir("certs")

    inputs.file("createKeys.sh")
    outputs.dir(certDir)

    workingDir(layout.projectDirectory)

    executable("./createKeys.sh")
    args(certDir)
    // commandLine("sh", "-c", "./createKeys.sh")
}
tasks.named<ProcessResources>("processResources").get().dependsOn("createRsaKeys")