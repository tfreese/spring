// Created: 11.03.2020
package de.freese.spring.rsocket.client;

import javax.annotation.Resource;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Profile;
import reactor.core.publisher.Hooks;

/**
 * https://github.com/benwilcock/spring-rsocket-demo
 *
 * @author Thomas Freese
 */
@SpringBootApplication
// (exclude =
// {
// ReactiveUserDetailsServiceAutoConfiguration.class,
// SecurityAutoConfiguration.class,
// ReactiveSecurityAutoConfiguration.class,
// RSocketSecurityAutoConfiguration.class
// })
@Profile("!test")
public class RsocketClientDemo implements ApplicationRunner
{
    /**
     * @param args String[]
     */
    public static void main(final String[] args)
    {
        // Fehlermeldung, wenn Client die Verbindung schliesst.
        // Nur einmalig definieren, sonst gibs mehrere Logs-Meldungen !!!
        // Hooks.onErrorDropped(th -> LOGGER.warn(th.getMessage()));
        Hooks.onErrorDropped(th -> {
            // Empty
        });

        SpringApplication.run(RsocketClientDemo.class, args);
    }

    /**
    *
    */
    @Resource
    private RSocketClient rSocketClient;

    /**
     * @see org.springframework.boot.ApplicationRunner#run(org.springframework.boot.ApplicationArguments)
     */
    @Override
    public void run(final ApplicationArguments args) throws Exception
    {
        this.rSocketClient.login("user", "pass");

        // TimeUnit.SECONDS.sleep(1);
        this.rSocketClient.fireAndForget();

        // TimeUnit.SECONDS.sleep(1);
        this.rSocketClient.parameter();
        //
        // TimeUnit.SECONDS.sleep(1);
        this.rSocketClient.requestResponse();
        //
        // TimeUnit.SECONDS.sleep(1);
        this.rSocketClient.error();

        // TimeUnit.SECONDS.sleep(1);
        this.rSocketClient.channel();

        // TimeUnit.SECONDS.sleep(1);
        this.rSocketClient.startStream();
        // TimeUnit.SECONDS.sleep(10);
        this.rSocketClient.stopStream();

        this.rSocketClient.logout(); // @PreDestroy
    }
}
