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
public class Shutdown
{
    /**
     * @param args String[]
     * @throws Exception Falls was schief geht.
     */
    public static void main(final String[] args) throws Exception
    {
        DefaultResourceLoader resourceLoader = new DefaultResourceLoader();
        Resource resource = resourceLoader.getResource("classpath:application.properties");
        // Resource resource = new FileSystemResource("appliation.properties");

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

        // RestTemplate restTemplate = new RestTemplate();
        // restTemplate.exchange(repository, HttpMethod.POST, null, Void.class);
        // restTemplate.postForLocation(repository, null);

        HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
        connection.setRequestMethod("POST");
        connection.getResponseCode();
        connection.disconnect();
    }

    /**
     * Erzeugt eine neue Instanz von {@link Shutdown}
     */
    public Shutdown()
    {
        super();
    }
}
