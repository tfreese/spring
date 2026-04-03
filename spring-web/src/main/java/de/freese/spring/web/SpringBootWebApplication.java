// Created: 10.06.2015
package de.freese.spring.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * <a href="http://localhost:8088/spring-web/static/index.html">demo.jsf</a>
 * <a href="http://localhost:8088/spring-web/demo.jsf">demo.jsf</a>
 * <a href="http://localhost:8088/spring-web/content/index.jsf?param1=t">index.jsf</a>
 *
 * @author Thomas Freese
 */
@SpringBootApplication(scanBasePackages = {"de.freese.spring.web", "de.jsf"})
// @SuppressWarnings("checkstyle:HideUtilityClassConstructor")
public final class SpringBootWebApplication {
    // extends SpringBootServletInitializer

    static void main(final String[] args) {
        SpringApplication.run(SpringBootWebApplication.class, args);
        // configureApplication(new SpringApplicationBuilder()).run(args);
    }

    // private static SpringApplicationBuilder configureApplication(final SpringApplicationBuilder builder) {
    //     // headless(false) für Desktop
    //     return builder.sources(JsfApplication.class).headless(false).registerShutdownHook(true); // .bannerMode(Banner.Mode.OFF);
    // }

    // @Override
    // protected SpringApplicationBuilder configure(final SpringApplicationBuilder builder) {
    //     return configureApplication(builder);
    // }

    private SpringBootWebApplication() {
        super();
    }
}
