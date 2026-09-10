// Execute Tasks in SubModule: gradle MODUL:clean build
plugins {
    id("de.freese.gradle.conventions").apply(false)
    id("org.openjfx.javafxplugin").apply(false)
    id("org.springframework.boot").apply(false)
}

allprojects {
    plugins.apply("base")
}

subprojects {
    plugins.apply("de.freese.gradle.conventions")
    plugins.apply("io.spring.dependency-management")

    extensions.configure(io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension::class.java) {
        imports {
            mavenBom("de.codecentric:spring-boot-admin-dependencies:" + property("version_springBootAdmin"))
            mavenBom("org.springframework.ai:spring-ai-bom:" + property("version_springAi"))
            mavenBom("org.springframework.cloud:spring-cloud-dependencies:" + property("version_springCloud"))
            // mavenBom(SpringBootPlugin.BOM_COORDINATES)
            mavenBom("org.springframework.boot:spring-boot-dependencies:" + property("version_springBoot"))
        }

        dependencies {
            dependency("com.atomikos:transactions-spring-boot4-starter:" + property("version_atomicosSpringBootStarter"))
            dependency("com.esotericsoftware:kryo:" + property("version_kryo"))

            dependencySet("com.github.tjake:" + property("version_jlama")) {
                entry("jlama-core")
                entry("jlama-native")
            }

            // dependency("de.javakaffee:kryo-serializers:" + property("version_kryoSerializers"))

            dependency("io.opentelemetry.proto:opentelemetry-proto:" + property("version_opentelemetryProto"))

            dependency("jakarta.faces:jakarta.faces-api:" + property("version_jakartaFacesApi"))
            dependency("jakarta.platform:jakarta.jakartaee-api:" + property("version_jakartaApi"))
            dependency("jakarta.platform:jakarta.jakartaee-web-api:" + property("version_jakartaApi"))

            dependencySet("org.apache.qpid:" + property("version_qpid")) {
                entry("qpid-broker") {
                    exclude("ch.qos.logback:logback-core")
                    exclude("ch.qos.logback:logback-classic")
                }
                entry("qpid-bdbstore") {
                    exclude("com.sleepycat:je")
                }
            }

            // dependencySet(group: "org.apache.tomcat", version: dependencyManagement.importedProperties["tomcat.version"]) {
            //     entry("tomcat-embed-jasper")
            // }

            dependency("org.glassfish:jakarta.faces:" + property("version_glassfishJakartaFaces"))

            dependency("org.joinfaces:primefaces-spring-boot-starter:" + property("version_joinfaces"))

            dependency("org.neo4j:neo4j:" + property("version_neo4j"))


            dependencySet("org.openjdk.jmh:" + property("version_jmh")) {
                entry("jmh-core")
                entry("jmh-generator-annprocess")
            }

            // dependency("org.primefaces:primefaces:" + property("version_primefaces"))
            // dependency("org.primefaces.extensions:primefaces-extensions:" + property("version_primefacesExtensions"))
            // dependency("org.primefaces:primefaces-themes:" + property("version_primefacesThemes"))
            dependency("org.springdoc:springdoc-openapi-starter-webmvc-ui:" + property("version_springDoc"))

            dependencySet("org.springframework.cloud:" + property("version_springCloudNetflix")) {
                entry("spring-cloud-starter-netflix-ribbon")
                entry("spring-cloud-starter-netflix-hystrix")
                entry("spring-cloud-starter-netflix-hystrix-dashboard")
            }

            dependency("software.xdev:chartjs-java-model:" + property("version_chartjsJavaModel"))
        }
    }

    plugins.withType<JavaPlugin> {
        val mockitoAgent = configurations.create("mockitoAgent")

        dependencies {
            // add("implementation", platform("de.freese:maven-bom:$version_mavenBom"))

            add("runtimeOnly", "org.springframework.boot:spring-boot-properties-migrator")

            // add("testImplementation", "com.google.code.findbugs:annotations:3.0.1") // SuppressFBWarnings
            add("testImplementation", "org.awaitility:awaitility")
            add("testImplementation", "org.junit.jupiter:junit-jupiter")
            add("testImplementation", "org.springframework.boot:spring-boot-starter-test")
            add("testRuntimeOnly", "org.junit.platform:junit-platform-launcher")

            add("testImplementation", "org.mockito:mockito-junit-jupiter")
            mockitoAgent("org.mockito:mockito-core") {
                isTransitive = false
            }
        }

        tasks.withType<Test>().configureEach {
            jvmArgs.add("-javaagent:${mockitoAgent.asPath}")
        }
    }
}
