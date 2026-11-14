plugins {
    id("java")
    id("org.springframework.boot")
}

description = "Messaging with JMS by ActiveMQ"

dependencies {
    implementation("org.apache.activemq:artemis-jakarta-server")
    implementation("org.springframework.boot:spring-boot-starter-json")
    implementation("org.springframework.boot:spring-boot-starter-artemis")
}

// Start: gradle bootRun --args="--spring.profiles.active=dev"
springBoot {
    mainClass.set("de.freese.spring.messaging.jms.SpringJmsApplication")
}
