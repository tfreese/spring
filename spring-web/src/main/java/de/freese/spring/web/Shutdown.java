// Created: 11.08.2016
package de.freese.spring.web;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Optional;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

/**
 * Sendet das shutdown-Signal.
 *
 * @author Thomas Freese
 */
final class Shutdown
{
    public static final Logger LOGGER = LoggerFactory.getLogger(Shutdown.class);

    public static void main(final String[] args) throws Exception
    {
        DefaultResourceLoader resourceLoader = new DefaultResourceLoader();
        Resource resource = resourceLoader.getResource("classpath:application.properties");
        // Resource resource = new FileSystemResource("application.properties");

        Properties props = new Properties();

        if (resource.isReadable())
        {
            try (InputStream inputStream = resource.getInputStream())
            {
                props.load(inputStream);
            }
        }

        boolean sslEnabled = Optional.ofNullable(props.getProperty("server.ssl.enabled")).map(Boolean::parseBoolean).orElse(false);
        String host = Optional.ofNullable(props.getProperty("server.address")).orElse("localhost");
        int port = Integer.parseInt(Optional.ofNullable(props.getProperty("local.server.port")).orElse(props.getProperty("server.port")));
        String contextPath = Optional.ofNullable(props.getProperty("server.servlet.context-path")).orElse("");
        String endPointPath = Optional.ofNullable(props.getProperty("management.endpoints.web.base-path")).orElse("");

        String url = "%s://%s:%d%s%s/shutdown".formatted(sslEnabled ? "https" : "http", host, port, contextPath, endPointPath);
        LOGGER.info("execute {}", url);
        
        // curl -X POST localhost:8088/spring-boot-web/actuator/shutdown
        URI uri = URI.create(url);

        HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
        connection.setRequestMethod("POST");
        connection.getResponseCode();
        connection.disconnect();
    }

    private Shutdown()
    {
        super();
    }
}
