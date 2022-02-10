// Created: 11.03.2020
package de.freese.spring.rsocket.client;

import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveUserDetailsServiceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.rsocket.RSocketSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Profile;

/**
 * https://github.com/benwilcock/spring-rsocket-demo
 *
 * @author Thomas Freese
 */
@SpringBootApplication(exclude =
{
        ReactiveUserDetailsServiceAutoConfiguration.class,
        SecurityAutoConfiguration.class,
        ReactiveSecurityAutoConfiguration.class,
        RSocketSecurityAutoConfiguration.class
})
@Profile("!test")
public class RsocketClientDemo implements ApplicationRunner
{
    /**
     * @param args String[]
     */
    public static void main(final String[] args)
    {
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
        this.rSocketClient.fireAndForget();
        this.rSocketClient.parameter();
        this.rSocketClient.requestResponse();
        this.rSocketClient.error();

        this.rSocketClient.startStream();
        TimeUnit.SECONDS.sleep(10);
        this.rSocketClient.stopStream();

        this.rSocketClient.logout();
    }
}
