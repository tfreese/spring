package de.freese.spring.jwt;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * <a href="https://github.com/murraco/spring-boot-jwt">spring-boot-jwt</a>
 *
 * @author Thomas Freese
 */
@SpringBootApplication
public class JwtAuthorisationApplication extends SpringBootServletInitializer {
    public static void main(final String[] args) {
        // ApplicationContext context = SpringApplication.run(SpringBootThymeleafApplication.class, args);
        configureApplication(new SpringApplicationBuilder()).run(args);
    }

    private static SpringApplicationBuilder configureApplication(final SpringApplicationBuilder builder) {
        // headless(false) für Desktop
        // .bannerMode(Banner.Mode.OFF);
        // .profiles(profiles)
        return builder.sources(JwtAuthorisationApplication.class).headless(true);// .profiles("with-ssl");
    }

    @Override
    protected SpringApplicationBuilder configure(final SpringApplicationBuilder application) {
        return configureApplication(application);
    }
}
