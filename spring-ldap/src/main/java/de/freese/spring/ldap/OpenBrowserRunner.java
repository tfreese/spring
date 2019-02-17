/**
 * Created: 12.09.2018
 */

package de.freese.spring.ldap;

import java.awt.Desktop;
import java.net.URI;
import java.net.URL;
import java.util.Optional;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * @author Thomas Freese
 */
@Component
@Profile("!test")
@Order(20)
public class OpenBrowserRunner implements CommandLineRunner
{
    /**
    *
    */
    public static final Logger LOGGER = LoggerFactory.getLogger(OpenBrowserRunner.class);

    /**
     * @param environment {@link Environment}
     * @return String
     */
    public static String getRootUri(final Environment environment)
    {
        int port =
                Optional.ofNullable(environment.getProperty("local.server.port", Integer.class)).orElse(environment.getProperty("server.port", Integer.class));
        Optional<String> contextPath = Optional.ofNullable(environment.getProperty("server.servlet.context-path", String.class));
        Optional<Boolean> sslEnabled = Optional.ofNullable(environment.getProperty("server.ssl.enabled", Boolean.class));

        String protocol = sslEnabled.orElse(false) ? "https" : "http";

        String rootUri = protocol + "://localhost:" + port + contextPath.orElse("");

        return rootUri;
    }

    /**
     *
     */
    @Resource
    private Environment environment = null;

    /**
     * Erstellt ein neues {@link OpenBrowserRunner} Object.
     */
    public OpenBrowserRunner()
    {
        super();
    }

    /**
     * @see org.springframework.boot.CommandLineRunner#run(java.lang.String[])
     */
    @Override
    public void run(final String...args) throws Exception
    {
        LOGGER.info("");

        String rootUri = getRootUri(this.environment);
        URL url = new URL(rootUri);
        URI uri = url.toURI();

        try
        {
            // Firefox: view-source:URI
            Runtime.getRuntime().exec(new String[]
            {
                    "C:\\Program Files (x86)\\Mozilla Firefox\\firefox.exe", "-new-tab", url.toString()
            });
        }
        catch (Exception ex)
        {
            try
            {
                // Linux
                Runtime.getRuntime().exec(new String[]
                {
                        "firefox", "-new-tab", url.toString()
                });
            }
            catch (Exception ex2)
            {
                // IE
                Desktop.getDesktop().browse(uri);
            }
        }
    }
}
