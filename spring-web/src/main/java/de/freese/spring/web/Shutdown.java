// Created: 11.08.2016
package de.freese.spring.web;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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
        URI uri = shutdownUriByProperties();

        if (uri == null)
        {
            return;
        }

        LOGGER.info("execute {}", uri);

        // curl -X POST localhost:8088/spring-boot-web/actuator/shutdown
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(""))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        LOGGER.info(response.body());

        //        HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
        //        connection.setRequestMethod("POST");
        //        connection.getResponseCode();
        //        connection.disconnect();
    }

    private static URI shutdownUriByProperties() throws Exception
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
        else
        {
            LOGGER.error("can not read: {}", resource.getFilename());
            return null;
        }

        boolean sslEnabled = Optional.ofNullable(props.getProperty("server.ssl.enabled")).map(Boolean::parseBoolean).orElse(false);
        String host = Optional.ofNullable(props.getProperty("server.address")).orElse("localhost");
        int port = Integer.parseInt(Optional.ofNullable(props.getProperty("local.server.port")).orElse(props.getProperty("server.port")));
        String contextPath = Optional.ofNullable(props.getProperty("server.servlet.context-path")).orElse("");
        String endPointPath = Optional.ofNullable(props.getProperty("management.endpoints.web.base-path")).orElse("");

        String url = "%s://%s:%d%s%s/shutdown".formatted(sslEnabled ? "https" : "http", host, port, contextPath, endPointPath);

        return URI.create(url);
    }

    private Shutdown()
    {
        super();
    }
}
