plugins {
    id("java")
}

description = "Kubernetes API"

dependencies {
    implementation("io.kubernetes:client-java:" + property("version_kubernetesClientJava"))

    runtimeOnly("org.slf4j:slf4j-simple")
}
