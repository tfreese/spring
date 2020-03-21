// Created: 14.02.2017
package de.freese.spring;

import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.gson.GsonAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * Startklasse des Servers.<br>
 *
 * @author Thomas Freese
 */
@SpringBootApplication(exclude =
{
        GsonAutoConfiguration.class, SecurityAutoConfiguration.class
}) // GSON hat Fehler verursacht -->
   //// @EnableEurekaClient
@EnableDiscoveryClient
@ComponentScan(basePackages =
{
        "de.freese.spring"
})
public class MicroServiceApplication extends SpringBootServletInitializer // extends WebSecurityConfigurerAdapter // implements WebMvcConfigurer
{
    /**
     * Konfiguriert die SpringApplication.
     *
     * @param builder {@link SpringApplicationBuilder}
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
    @SuppressWarnings("resource")
    public static void main(final String[] args)
    {
        configureApplication(new SpringApplicationBuilder()).run(args);
    }

    /**
     * Erzeugt eine neue Instanz von {@link MicroServiceApplication}
     */
    public MicroServiceApplication()
    {
        super();
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
