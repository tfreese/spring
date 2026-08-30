package de.freese.spring.web;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.function.UnaryOperator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.PropertySourcesPropertyResolver;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

/**
 * Send the shutdown-Signal.
 *
 * @author Thomas Freese
 * @since 11.08.2016
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
        catch (final Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    private static URI createShutdownUri(final UnaryOperator<String> properties) {
        final boolean sslEnabled = Optional.ofNullable(properties.apply("server.ssl.enabled")).map(Boolean::parseBoolean).orElse(false);
        final String host = Optional.ofNullable(properties.apply("server.address")).orElse("localhost");
        final String portProperty = Optional.ofNullable(properties.apply("local.server.port")).orElse(properties.apply("server.port"));
        final int port = portProperty.contains(":") ? Integer.parseInt(portProperty.replace("}", "").split(":")[1]) : Integer.parseInt(portProperty);
        final String contextPath = Optional.ofNullable(properties.apply("server.servlet.context-path")).orElse("");
        final String endPointPath = Optional.ofNullable(properties.apply("management.endpoints.web.base-path")).orElse("");

        final String url = "%s://%s:%d%s%s/shutdown".formatted(sslEnabled ? "https" : "http", host, port, contextPath, endPointPath);

        return URI.create(url);
    }

    private static URI parseApplicationProperties() throws IOException {
        final DefaultResourceLoader resourceLoader = new DefaultResourceLoader();
        final Resource resource = resourceLoader.getResource("classpath:application.properties");
        // Resource resource = new FileSystemResource("application.properties");

        if (resource.isReadable()) {
            final Properties properties = new Properties();

            try (InputStream inputStream = resource.getInputStream()) {
                properties.load(inputStream);
            }

            return createShutdownUri(properties::getProperty);
        }
        else {
            LOGGER.error("can not read: {}", resource.getFilename());
        }

        return null;
    }

    private static URI parseApplicationYaml() throws IOException {
        final Resource resource = new ClassPathResource("application.yml");

        if (resource.isReadable()) {
            System.setProperty("spring.profiles.active", "shutdown");

            // 1. YAML einlesen.
            final YamlPropertySourceLoader loader = new YamlPropertySourceLoader();
            final List<PropertySource<?>> propertySources = loader.load("applicationYaml", resource);

            // 2. Environment aufbauen.
            final StandardEnvironment environment = new StandardEnvironment();
            propertySources.forEach(environment.getPropertySources()::addLast);

            // 3. Resolver für Platzhalter nutzen.
            final PropertySourcesPropertyResolver resolver = new PropertySourcesPropertyResolver(environment.getPropertySources());
            resolver.setIgnoreUnresolvableNestedPlaceholders(true);

            // final Set<String> allKeys = new HashSet<>();
            //
            // // Iteriere über alle registrierten Quellen im Environment.
            // for (PropertySource<?> source : environment.getPropertySources()) {
            //     // Nur Quellen, die ihre Keys "kennen" (EnumerablePropertySource)
            //     if (source instanceof EnumerablePropertySource) {
            //         final String[] propertyNames = ((EnumerablePropertySource<?>) source).getPropertyNames();
            //
            //         Collections.addAll(allKeys, propertyNames);
            //     }
            // }

            // Reines lesen der YAML.
            // final YamlPropertiesFactoryBean yamlFactory = new YamlPropertiesFactoryBean();
            // yamlFactory.setResources(resource);
            // Properties properties = Objects.requireNonNull(yamlFactory.getObject());

            return createShutdownUri(resolver::getProperty);
        }
        else {
            LOGGER.error("can not read: {}", resource.getFilename());
        }

        return null;
    }

    private static void shutdown() throws IOException, InterruptedException {
        URI uri = parseApplicationYaml();

        if (uri == null) {
            uri = parseApplicationProperties();
        }

        if (uri == null) {
            LOGGER.warn("Failed to read application URI.");
            return;
        }

        LOGGER.info("execute: {}", uri);

        String response = null;

        // curl -X POST localhost:8088/spring-web/actuator/shutdown
        try (HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(3L))
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
