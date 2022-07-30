// Created: 14.02.2017
package de.freese.spring.cloud.microservice;

import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Startklasse des Servers.<br>
 *
 * @author Thomas Freese
 */
@SpringBootApplication
@EnableDiscoveryClient
public class MicroServiceApplication extends SpringBootServletInitializer
{
    /**
     * Konfiguriert die SpringApplication.
     *
     * @param builder {@link SpringApplicationBuilder}
     *
     * @return {@link SpringApplicationBuilder}
     */
    private static SpringApplicationBuilder configureApplication(final SpringApplicationBuilder builder)
    {
        //@formatter:off
        return builder
            .sources(MicroServiceApplication.class)
            .bannerMode(Banner.Mode.OFF)
            .headless(true)
            .registerShutdownHook(true);
        //@formatter:on
        // .listeners(new ApplicationPidFileWriter("spring-boot-web.pid"))
        // .web(false)
    }

    // static
    // {
    // System.setProperty("server.port", Integer.toString(65501));
    // }

    /**
     * @param args String[]
     */
    public static void main(final String[] args) throws InterruptedException
    {
        configureApplication(new SpringApplicationBuilder()).run(args);

        //        SpringApplication.run(MicroServiceApplication.class, args);
    }

    /**
     * POM:<br>
     * &lt;packaging>&gt;war&lt;/packaging&gt;<<br>
     * Tomcat aus spring-boot-starter-web excludieren und explizit auf provided setzen.<br>
     * Alle anderen J2EE-Jars auf provided setzen.
     *
     * @see org.springframework.boot.web.servlet.support.SpringBootServletInitializer#configure(org.springframework.boot.builder.SpringApplicationBuilder)
     */
    @Override
    protected SpringApplicationBuilder configure(final SpringApplicationBuilder application)
    {
        return configureApplication(application);
    }
}