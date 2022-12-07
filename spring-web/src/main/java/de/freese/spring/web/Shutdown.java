// Created: 11.08.2016
package de.freese.spring.web;

import java.io.InputStream;
import java.net.ProxySelector;
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
 * Sendet das shutdown-Signal.
 *
 * @author Thomas Freese
 */
final class Shutdown
{
    public static final Logger LOGGER = LoggerFactory.getLogger(Shutdown.class);

    public static void main(final String[] args) throws Exception
    {
        URI uri = parseApplicationProperties();

        if (uri == null)
        {
            uri = parseApplicationYaml();
        }

        if (uri == null)
        {
            return;
        }

        LOGGER.info("execute {}", uri);

        // curl -X POST localhost:8088/spring-boot-web/actuator/shutdown
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .followRedirects(HttpClient.Redirect.NEVER)
                .proxy(ProxySelector.getDefault())
                .connectTimeout(Duration.ofSeconds(3))
                //.executor(executorServiceHttpClient)
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.noBody())
                .header("user-agent", "Java")
                .build();

        String response = null;

        HttpResponse<String> httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        response = httpResponse.body();

        // REST-Template
        //       HttpHeaders headers = new HttpHeaders();
        //       headers.setContentType(MediaType.APPLICATION_JSON);
        //       HttpEntity<String> entity = new HttpEntity<>(null, headers);
        //
        //        response = new RestTemplate().postForEntity(shutdownUri, entity, String.class).getBody();
        //
        // PLAIN
        //        HttpURLConnection connection = (HttpURLConnection) shutdownUri.toURL().openConnection();
        //        connection.setRequestMethod("POST");
        //
        //        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
        //            response = br.lines().collect(Collectors.joining(System.lineSeparator()));
        //        }
        //
        //        connection.disconnect();

        LOGGER.info(response);
    }

    private static URI parseApplicationProperties() throws Exception
    {
        DefaultResourceLoader resourceLoader = new DefaultResourceLoader();
        Resource resource = resourceLoader.getResource("classpath:application.properties");
        // Resource resource = new FileSystemResource("application.properties");

        Properties properties = new Properties();

        if (resource.isReadable())
        {
            try (InputStream inputStream = resource.getInputStream())
            {
                properties.load(inputStream);
            }
        }
        else
        {
            LOGGER.error("can not read: {}", resource.getFilename());
            return null;
        }

        return parseShutdownUri(properties);
    }

    private static URI parseApplicationYaml() throws Exception
    {
        Resource resource = new ClassPathResource("application.yml");

        Properties properties = null;

        if (resource.isReadable())
        {
            System.setProperty("spring.profiles.active", "shutdown");

            YamlPropertiesFactoryBean yamlFactory = new YamlPropertiesFactoryBean();
            yamlFactory.setResources(resource);
            properties = Objects.requireNonNull(yamlFactory.getObject());
        }
        else
        {
            LOGGER.error("can not read: {}", resource.getFilename());
            return null;
        }

        return parseShutdownUri(properties);
    }

    private static URI parseShutdownUri(Properties properties)
    {
        boolean sslEnabled = Optional.ofNullable(properties.getProperty("server.ssl.enabled")).map(Boolean::parseBoolean).orElse(false);
        String host = Optional.ofNullable(properties.getProperty("server.address")).orElse("localhost");
        int port = Integer.parseInt(Optional.ofNullable(properties.getProperty("local.server.port")).orElse(properties.getProperty("server.port")));
        String contextPath = Optional.ofNullable(properties.getProperty("server.servlet.context-path")).orElse("");
        String endPointPath = Optional.ofNullable(properties.getProperty("management.endpoints.web.base-path")).orElse("");

        String url = "%s://%s:%d%s%s/shutdown".formatted(sslEnabled ? "https" : "http", host, port, contextPath, endPointPath);

        return URI.create(url);
    }

    private Shutdown()
    {
        super();
    }
}
