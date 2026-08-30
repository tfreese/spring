package de.freese.spring.cloud.client;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * @author Thomas Freese
 * @since 14.02.2017
 */
@SpringBootApplication
// @SuppressWarnings("checkstyle:HideUtilityClassConstructor")
public class ClientManuell {
    static void main() {
        new SpringApplicationBuilder(ClientManuell.class).run("--spring.profiles.active=manuell");
    }

    // private ClientManuell() {
    //     super();
    // }
}
