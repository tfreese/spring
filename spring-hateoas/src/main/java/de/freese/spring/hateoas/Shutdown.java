// Created: 11.08.2016
package de.freese.spring.hateoas;

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
public final class Shutdown {
    public static void main(final String[] args) throws Exception {
        final DefaultResourceLoader resourceLoader = new DefaultResourceLoader();
        final Resource resource = resourceLoader.getResource("classpath:application.properties");
        // Resource resource = new FileSystemResource("application.properties");

        final Properties props = new Properties();

        if (resource.isReadable()) {
            try (InputStream inputStream = resource.getInputStream()) {
                props.load(inputStream);
            }
        }

        final int port = Integer.parseInt(props.getProperty("server.port"));
        final Optional<String> contextPath = Optional.ofNullable(props.getProperty("server.servlet.context-path"));

        final URI uri = URI.create("http://localhost:" + port + contextPath.orElse("") + "/actuator/shutdown");

        // final RestTemplate restTemplate = new RestTemplate();
        // restTemplate.exchange(repository, HttpMethod.POST, null, Void.class);
        // restTemplate.postForLocation(repository, null);

        final HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
        connection.setRequestMethod("POST");
        connection.getResponseCode();
        connection.disconnect();
    }

    private Shutdown() {
        super();
    }
}
