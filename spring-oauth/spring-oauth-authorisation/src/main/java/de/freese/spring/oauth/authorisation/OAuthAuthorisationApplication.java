package de.freese.spring.oauth.authorisation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;

/**
 * @author Thomas Freese
 */
@SpringBootApplication
@EnableResourceServer
public class OAuthAuthorisationApplication extends SpringBootServletInitializer
{
    /**
    *
    */
    public static final Logger LOGGER = LoggerFactory.getLogger(OAuthAuthorisationApplication.class);

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
        return builder.sources(OAuthAuthorisationApplication.class).headless(true);// .profiles("with-ssl");
    }

    /**
     * @param args String[]
     * @throws Exception Falls was schief geht.
     */
    public static void main(final String[] args) throws Exception
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