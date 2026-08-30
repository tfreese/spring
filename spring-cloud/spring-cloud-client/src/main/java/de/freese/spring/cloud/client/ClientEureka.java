package de.freese.spring.cloud.client;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * @author Thomas Freese
 * @since 14.02.2017
 */
@SpringBootApplication
// @SuppressWarnings("checkstyle:HideUtilityClassConstructor")
public class ClientEureka {
    static void main() {
        new SpringApplicationBuilder(ClientEureka.class).run("--spring.profiles.active=eureka");
    }

    // private ClientEureka() {
    //     super();
    // }
}
