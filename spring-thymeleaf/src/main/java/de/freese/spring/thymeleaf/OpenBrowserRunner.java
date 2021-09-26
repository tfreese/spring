// Created: 12.09.2018
package de.freese.spring.thymeleaf;

import java.awt.Desktop;
import java.net.URI;

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
    private Environment environment;

    /**
     * google-chrome-stable --disk-cache-dir=/tmp/.chrome/cache --media-cache-dir=/tmp/.chrome/cache_media %U
     *
     * @param url String
     *
     * @throws Exception Falls was schief geht.
     */
    private void openLinuxChrome(final String url) throws Exception
    {
        Runtime.getRuntime().exec(new String[]
        {
                "google-chrome-stable", url
        });
    }

    /**
     * chromium %U --disk-cache-dir=/tmp/.chrome/cache --media-cache-dir=/tmp/.chrome/cache_media
     *
     * @param url String
     *
     * @throws Exception Falls was schief geht.
     */
    private void openLinuxChromium(final String url) throws Exception
    {
        Runtime.getRuntime().exec(new String[]
        {
                "chromium", url
        });
    }

    /**
     * @param url String
     *
     * @throws Exception Falls was schief geht.
     */
    private void openLinuxFirefox(final String url) throws Exception
    {
        Runtime.getRuntime().exec(new String[]
        {
                "firefox", "-new-tab", url
        });
    }

    /**
     * Firefox: view-source:URI
     *
     * @param url String
     *
     * @throws Exception Falls was schief geht.
     */
    private void openWindowsFirefox(final String url) throws Exception
    {
        Runtime.getRuntime().exec(new String[]
        {
                "C:\\Program Files (x86)\\Mozilla Firefox\\firefox.exe", "-new-tab", url
        });
    }

    /**
     * @see org.springframework.boot.CommandLineRunner#run(java.lang.String[])
     */
    @Override
    public void run(final String...args) throws Exception
    {
        LOGGER.info("");

        String rootUri = ThymeleafApplication.getRootUri(this.environment);
        URI uri = URI.create(rootUri);

        try
        {
            openLinuxChrome(uri.toString());

        }
        catch (Exception ex)
        {
            try
            {
                openLinuxFirefox(uri.toString());
            }
            catch (Exception ex2)
            {
                try
                {
                    openLinuxChromium(uri.toString());
                }
                catch (Exception ex3)
                {
                    try
                    {
                        openWindowsFirefox(uri.toString());
                    }
                    catch (Exception ex4)
                    {
                        // System-Default
                        Desktop.getDesktop().browse(uri);
                    }
                }
            }
        }
    }
}
