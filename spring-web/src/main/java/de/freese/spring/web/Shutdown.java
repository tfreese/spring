// Created: 11.08.2016
package de.freese.spring.web;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Optional;
import java.util.Properties;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

/**
 * Sendet das shutdown-Signal.
 *
 * @author Thomas Freese
 */
public final class Shutdown
{
    /**
     * @param args String[]
     *
     * @throws Exception Falls was schiefgeht.
     */
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

        int port = Integer.parseInt(props.getProperty("server.port"));
        Optional<String> contextPath = Optional.ofNullable(props.getProperty("server.servlet.context-path"));

        // curl -X POST localhost:8088/spring-boot-web/actuator/shutdown
        URI uri = URI.create("http://localhost:" + port + contextPath.orElse("") + "/actuator/shutdown");

        HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
        connection.setRequestMethod("POST");
        connection.getResponseCode();
        connection.disconnect();
    }

    /**
     * Erzeugt eine neue Instanz von {@link Shutdown}
     */
    private Shutdown()
    {
        super();
    }
}
