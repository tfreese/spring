plugins {
    id("java")
    id("org.openjfx.javafxplugin")
    id("org.springframework.boot")
}

description = "spring with javafx"

// For JavaFx native-Library Downloads.
// configurations.matching { it.canBeResolved }.configureEach {
//     attributes {
//         attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage, Usage.JAVA_RUNTIME))
//         attribute(OperatingSystemFamily.OPERATING_SYSTEM_ATTRIBUTE, objects.named(OperatingSystemFamily, OperatingSystemFamily.LINUX))
//         attribute(MachineArchitecture.ARCHITECTURE_ATTRIBUTE, objects.named(MachineArchitecture, MachineArchitecture.X86_64))
//     }
// }

javafx {
    version = property("version_javafx").toString()
    modules = listOf("javafx.controls", "javafx.fxml")
    configuration = "implementation"
    setPlatform("linux") // linux, windows, mac
    // sdk = "PATH"
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
}

// Start: gradle bootRun --args="--spring.profiles.active=dev"
springBoot {
    mainClass.set("de.freese.spring.javafx.JavaFxApplicationLauncher")
}
