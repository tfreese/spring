package de.freese.spring.oauth2.authorisation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.util.SocketUtils;

/**
 * @author Thomas Freese
 */
@SpringBootApplication
@ComponentScan(basePackages =
{
        "de.freese.spring"
})
public class OAuth2AuthorisationServerApplication extends SpringBootServletInitializer
{
    /**
    *
    */
    public static final Logger LOGGER = LoggerFactory.getLogger(OAuth2AuthorisationServerApplication.class);

    static
    {
        int port = SocketUtils.findAvailableTcpPort();

        // Damit die Placeholder in Properties funktionieren: ${hsqldb.server.port}
        System.setProperty("hsqldb.server.port", Integer.toString(port));
    }

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
        return builder.sources(OAuth2AuthorisationServerApplication.class).headless(true);
    }

    /**
     * @param args String[]
     */
    @SuppressWarnings("resource")
    public static void main(final String[] args)
    {
        // ApplicationContext context = SpringApplication.run(OAuth2AuthorisationServerApplication.class, args);
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
