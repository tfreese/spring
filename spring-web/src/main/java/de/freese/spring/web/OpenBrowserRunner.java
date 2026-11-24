package de.freese.spring.web;

import java.awt.Desktop;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.net.URI;
import java.util.Optional;

import jakarta.annotation.Resource;

import org.jspecify.annotations.NonNull;
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
 * @since 12.09.2018
 */
@Component
@Profile({"!test & !shutdown"})
@Order(1)
public class OpenBrowserRunner implements ApplicationRunner {
    public static final Logger LOGGER = LoggerFactory.getLogger(OpenBrowserRunner.class);

    private static void openWithDesktop(final URI uri) {
        if (GraphicsEnvironment.isHeadless() || !Desktop.isDesktopSupported()) {
            LOGGER.warn("No graphical desktop is available; browser URL: {}", uri);
            return;
        }

        final Desktop desktop = Desktop.getDesktop();

        if (!desktop.isSupported(Desktop.Action.BROWSE)) {
            LOGGER.warn("Desktop BROWSE action is not supported on this platform; browser URL: {}", uri);
            return;
        }

        try {
            desktop.browse(uri);
        }
        catch (final IOException | UnsupportedOperationException ex) {
            LOGGER.warn("Could not open browser URL {}: {}", uri, ex.getMessage());
        }
    }

    private static boolean openWithProcess(final URI uri) {
        final String os = System.getProperty("os.name", "").toLowerCase();

        final String[][] commands;

        if (os.contains("win")) {
            commands = new String[][]{{"rundll32", "url.dll,FileProtocolHandler", uri.toString()}};
        }
        else if (os.contains("mac") || os.contains("darwin")) {
            commands = new String[][]{{"open", uri.toString()}};
        }
        else {
            commands = new String[][]{
                    {"xdg-open", uri.toString()},
                    {"google-chrome-stable", uri.toString()},
                    {"chromium", uri.toString()},
                    {"firefox", "-new-tab", uri.toString()}
            };
        }

        for (final String[] command : commands) {
            try {
                try (Process process = new ProcessBuilder(command)
                        .redirectErrorStream(true)
                        .start()) {
                    process.pid();
                }
                
                return true;
            }
            catch (final IOException _) {
                // Try the next browser/launcher.
            }
        }

        return false;
    }

    @Resource
    private PropertyResolver propertyResolver;

    @Override
    public void run(final @NonNull ApplicationArguments args) {
        if (Boolean.FALSE.equals(propertyResolver.getProperty("app.open-browser.enabled", boolean.class, true))) {
            LOGGER.debug("Opening a browser is disabled");
            return;
        }

        final boolean sslEnabled = Optional.ofNullable(propertyResolver.getProperty("server.ssl.enabled", boolean.class)).orElse(false);
        final String host = Optional.ofNullable(propertyResolver.getProperty("server.address")).orElse("localhost");
        final int port = Optional.ofNullable(propertyResolver.getProperty("local.server.port", int.class))
                .orElse(propertyResolver.getProperty("server.port", int.class, 0));
        final String contextPath = Optional.ofNullable(propertyResolver.getProperty("server.servlet.context-path")).orElse("");
        final URI uri = URI.create("%s://%s:%d%s/demo.xhtml".formatted(sslEnabled ? "https" : "http", host, port, contextPath));

        LOGGER.info("Opening browser URL: {}", uri);

        if (!openWithProcess(uri)) {
            openWithDesktop(uri);
        }
    }
}