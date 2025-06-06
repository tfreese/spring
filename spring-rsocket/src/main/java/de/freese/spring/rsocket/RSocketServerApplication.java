// Created: 11.03.2020
package de.freese.spring.rsocket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Hooks;

/***
 * https:// github.com/benwilcock/spring-rsocket-demo
 **
 * @author Thomas Freese
 */
@SpringBootApplication
// (scanBasePackages = {
// "de.freese.spring.rsocket"
// })
@SuppressWarnings("checkstyle:HideUtilityClassConstructor")
public class RSocketServerApplication {
    public static void main(final String[] args) {
        // Fehlermeldung, wenn Client die Verbindung schliesst.
        // Nur einmalig definieren, sonst gib es mehrere Logs-Meldungen!
        // Hooks.onErrorDropped(th -> LOGGER.warn(th.getMessage()));
        Hooks.onErrorDropped(th -> {
            // Empty
        });

        SpringApplication.run(RSocketServerApplication.class, args);
    }
}
