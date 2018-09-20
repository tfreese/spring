/**
 * Created: 12.09.2018
 */

package de.freese.spring.thymeleaf;

import java.awt.Desktop;
import java.net.URI;
import java.net.URL;
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
@Order(1)
public class OpenBrowserRunner implements CommandLineRunner
{
    /**
    *
    */
    public static final Logger LOGGER = LoggerFactory.getLogger(OpenBrowserRunner.class);

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

        String rootUri = ThymeleafApplication.getRootUri(this.environment);
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
