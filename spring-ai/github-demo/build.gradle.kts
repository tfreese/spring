import org.gradle.internal.classpath.Instrumented.systemProperty

plugins {
    id("java")
    id("org.springframework.boot")
}

description = "spring-ai example"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.ai:spring-ai-starter-model-openai")
    // implementation("org.springframework.ai:spring-ai-starter-model-anthropic")
}

springBoot {
    mainClass.set("de.spring.ai.simple.GithubApplication")
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    systemProperty("openai.logging", "body")
}