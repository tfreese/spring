package de.freese.spring.jwt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * <a href="https://github.com/murraco/spring-boot-jwt">spring-boot-jwt</a>
 *
 * @author Thomas Freese
 */
@SpringBootApplication
public class JwtAuthorisationApplication extends SpringBootServletInitializer
{
    /**
    *
    */
    public static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthorisationApplication.class);

    /**
     * Konfiguriert die SpringApplication.
     *
     * @param builder {@link SpringApplicationBuilder}
     * @return {@link SpringApplicationBuilder}
     */
    private static SpringApplicationBuilder configureApplication(final SpringApplicationBuilder builder)
    {
        // headless(false) für Desktop
        // .bannerMode(Banner.Mode.OFF);
        // .profiles(profiles)
        return builder.sources(JwtAuthorisationApplication.class).headless(true);// .profiles("with-ssl");
    }

    /**
     * @param args String[]
     */
    public static void main(final String[] args)
    {
        // ApplicationContext context = SpringApplication.run(SpringBootThymeleafApplication.class, args);
        configureApplication(new SpringApplicationBuilder()).run(args);
    }

    /**
     * @see org.springframework.boot.web.servlet.support.SpringBootServletInitializer#configure(org.springframework.boot.builder.SpringApplicationBuilder)
     */
    @Override
    protected SpringApplicationBuilder configure(final SpringApplicationBuilder application)
    {
        return configureApplication(application);
    }
}
