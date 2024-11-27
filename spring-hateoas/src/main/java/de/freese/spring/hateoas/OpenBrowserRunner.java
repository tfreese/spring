// Created: 12.09.2018
package de.freese.spring.hateoas;

import java.awt.Desktop;
import java.net.URI;
import java.util.Optional;

import jakarta.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.PropertyResolver;
import org.springframework.stereotype.Component;

/**
 * @author Thomas Freese
 */
@Component
@Profile("!test")
@Order(1)
public class OpenBrowserRunner implements ApplicationRunner {
    public static final Logger LOGGER = LoggerFactory.getLogger(OpenBrowserRunner.class);

    public static String getRootUri(final PropertyResolver propertyResolver) {
        final int port = Optional.ofNullable(propertyResolver.getProperty("local.server.port", Integer.class))
                .or(() -> Optional.ofNullable(propertyResolver.getProperty("server.port", Integer.class)))
                .orElse(0);
        final Optional<String> contextPath = Optional.ofNullable(propertyResolver.getProperty("server.servlet.context-path", String.class));
        final Optional<Boolean> sslEnabled = Optional.ofNullable(propertyResolver.getProperty("server.ssl.enabled", Boolean.class));

        final String protocol = sslEnabled.orElse(false) ? "https" : "http";

        String rootUri = protocol + "://localhost:" + port + contextPath.orElse("");
        rootUri += "/greeter";

        return rootUri;
    }

    /**
     * google-chrome-stable --disk-cache-dir=/tmp/.chrome/cache --media-cache-dir=/tmp/.chrome/cache_media %U
     */
    private static void openLinuxChrome(final String url) throws Exception {
        Runtime.getRuntime().exec(new String[]{"google-chrome-stable", url});
    }

    /**
     * chromium %U --disk-cache-dir=/tmp/.chrome/cache --media-cache-dir=/tmp/.chrome/cache_media
     */
    private static void openLinuxChromium(final String url) throws Exception {
        Runtime.getRuntime().exec(new String[]{"chromium", url});
    }

    private static void openLinuxFirefox(final String url) throws Exception {
        Runtime.getRuntime().exec(new String[]{"firefox", "-new-tab", url});
    }

    /**
     * Firefox: view-source:URI
     */
    private static void openWindowsFirefox(final String url) throws Exception {
        Runtime.getRuntime().exec(new String[]{"C:\\Program Files (x86)\\Mozilla Firefox\\firefox.exe", "-new-tab", url});
    }

    @Resource
    private PropertyResolver propertyResolver;

    @Override
    public void run(final ApplicationArguments args) throws Exception {
        LOGGER.info("");

        final String rootUri = getRootUri(propertyResolver);
        final URI uri = URI.create(rootUri);

        try {
            openLinuxChrome(uri.toString());

        }
        catch (Exception ex) {
            try {
                openLinuxFirefox(uri.toString());
            }
            catch (Exception ex2) {
                try {
                    openLinuxChromium(uri.toString());
                }
                catch (Exception ex3) {
                    try {
                        openWindowsFirefox(uri.toString());
                    }
                    catch (Exception ex4) {
                        // System-Default
                        Desktop.getDesktop().browse(uri);
                    }
                }
            }
        }
    }
}
