/**
 * Created: 31.10.2019
 */

package de.freese.spring.oauth2.client.ui;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * @author Thomas Freese
 */
@SpringBootApplication
public class OAuth2ClientUiApplication extends SpringBootServletInitializer
{
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
        return builder.sources(OAuth2ClientUiApplication.class).headless(true);// .profiles("with-ssl");
    }

    /**
     * @param args String[]
     * @throws Exception Falls was schief geht.
     */
    public static void main(final String[] args) throws Exception
    {
        // ApplicationContext context = SpringApplication.run(OAuth2ClientUiApplication.class, args);
        configureApplication(new SpringApplicationBuilder()).run(args);
    }

    /**
     * Erstellt ein neues {@link OAuth2ClientUiApplication} Object.
     */
    public OAuth2ClientUiApplication()
    {
        super();
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
