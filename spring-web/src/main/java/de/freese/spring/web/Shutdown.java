// Created: 11.08.2016
package de.freese.spring.web;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

/**
 * Send the shutdown-Signal.
 *
 * @author Thomas Freese
 */
final class Shutdown {
    public static final Logger LOGGER = LoggerFactory.getLogger(Shutdown.class);

    static void main() {
        try {
            shutdown();
        }
        catch (InterruptedException _) {
            // Restore interrupted state.
            Thread.currentThread().interrupt();
        }
        catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    private static URI createShutdownUri(final Properties properties) {
        final boolean sslEnabled = Optional.ofNullable(properties.getProperty("server.ssl.enabled")).map(Boolean::parseBoolean).orElse(false);
        final String host = Optional.ofNullable(properties.getProperty("server.address")).orElse("localhost");
        final int port = Integer.parseInt(Optional.ofNullable(properties.getProperty("local.server.port")).orElse(properties.getProperty("server.port")));
        final String contextPath = Optional.ofNullable(properties.getProperty("server.servlet.context-path")).orElse("");
        final String endPointPath = Optional.ofNullable(properties.getProperty("management.endpoints.web.base-path")).orElse("");

        final String url = "%s://%s:%d%s%s/shutdown".formatted(sslEnabled ? "https" : "http", host, port, contextPath, endPointPath);

        return URI.create(url);
    }

    private static URI parseApplicationProperties() throws IOException {
        final DefaultResourceLoader resourceLoader = new DefaultResourceLoader();
        final Resource resource = resourceLoader.getResource("classpath:application.properties");
        // Resource resource = new FileSystemResource("application.properties");

        final Properties properties = new Properties();

        if (resource.isReadable()) {
            try (InputStream inputStream = resource.getInputStream()) {
                properties.load(inputStream);
            }
        }
        else {
            LOGGER.error("can not read: {}", resource.getFilename());
            return null;
        }

        return createShutdownUri(properties);
    }

    private static URI parseApplicationYaml() {
        final Resource resource = new ClassPathResource("application.yml");

        Properties properties = null;

        if (resource.isReadable()) {
            System.setProperty("spring.profiles.active", "shutdown");

            final YamlPropertiesFactoryBean yamlFactory = new YamlPropertiesFactoryBean();
            yamlFactory.setResources(resource);
            properties = Objects.requireNonNull(yamlFactory.getObject());
        }
        else {
            LOGGER.error("can not read: {}", resource.getFilename());
            return null;
        }

        return createShutdownUri(properties);
    }

    private static void shutdown() throws IOException, InterruptedException {
        URI uri = parseApplicationYaml();

        if (uri == null) {
            uri = parseApplicationProperties();
        }

        if (uri == null) {
            return;
        }

        LOGGER.info("execute: {}", uri);

        String response = null;

        // curl -X POST localhost:8088/spring-web/actuator/shutdown
        try (HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(3))
                .build()) {
            final HttpRequest request = HttpRequest.newBuilder().uri(uri).POST(HttpRequest.BodyPublishers.noBody()).header("user-agent", "Java").build();

            final HttpResponse<String> httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            response = httpResponse.body();
        }

        // REST-Template
        // final HttpHeaders headers = new HttpHeaders();
        // headers.setContentType(MediaType.APPLICATION_JSON);
        // final HttpEntity<String> entity = new HttpEntity<>(null, headers);
        //
        // response = new RestTemplate().postForEntity(shutdownUri, entity, String.class).getBody();
        //
        // PLAIN
        // final HttpURLConnection connection = (HttpURLConnection) shutdownUri.toURL().openConnection();
        // connection.setRequestMethod("POST");
        //
        // try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
        //     response = br.lines().collect(Collectors.joining(System.lineSeparator()));
        // }
        //
        // connection.disconnect();

        LOGGER.info(response);
    }

    private Shutdown() {
        super();
    }
}
